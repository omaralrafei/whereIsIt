package org.med.darknetandroid;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.ContentValues.TAG;

public class AddItemActivity extends AppCompatActivity {

    ActivityResultLauncher<String> croppedImageResult;
    ActivityResultLauncher<Intent> coordinates;
    List<Items> itemsList = new ArrayList<>();
    List<ImageData> itemsImageData = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        //Creating references for the views to be used
        Button addItemButton = findViewById(R.id.add_item_add_button);
        Button submitButton = findViewById(R.id.add_item_submit_button);
        Button cancelButton = findViewById(R.id.add_item_cancel_button);
        final EditText nameEditText = findViewById(R.id.item_name_edit_text);
        TextView textView = findViewById(R.id.add_item_text_view);
        //Disabling the text view as it is not needed at first
        nameEditText.setEnabled(true);
        nameEditText.setVisibility(View.VISIBLE);
        textView.setEnabled(false);
        textView.setVisibility(View.INVISIBLE);

        //Begin the process of adding an item if an item name has been inserted
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nameEditText.getText().toString().equalsIgnoreCase("")){
                    Toast.makeText(AddItemActivity.this, "Please Insert Name of Item", Toast.LENGTH_SHORT).show();
                }else {
                    TextView textView = findViewById(R.id.add_item_text_view);
                    textView.setText(nameEditText.getText().toString());
                    nameEditText.setEnabled(false);
                    nameEditText.setVisibility(View.INVISIBLE);
                    textView.setEnabled(true);
                    textView.setVisibility(View.VISIBLE);
                    //Launches an activity to select an image
                    croppedImageResult.launch("image/*");
                }
            }
        });

        //get the result of the image selected and start another activity to crop it
        croppedImageResult =registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                String dest_uri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
                UCrop.of(result, Uri.fromFile(new File(getExternalMediaDirs()[0],dest_uri))).withAspectRatio(1,1).start(AddItemActivity.this);
            }
        });

        //if there are pictures added, and in turn items added to the list, then begin the process of
        // uploading these images along with their bounding boxes to the server
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemsList.size() > 0){
                    uploadImages();
                }else
                {
                    Toast.makeText(AddItemActivity.this, "No images were added!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Cancel button if the user decides to not add the item
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent welcome = new Intent(AddItemActivity.this, WelcomeActivity.class);
                startActivity(welcome);
                finish();
            }
        });
    }


    //Using the Retrofit2 library, this method uploads the images stored in the itemsList as well as their bounding boxes and label name.
    //It leverages the Network client as well as the UploadAPIs to construct a post method that accepts an array of Parts containing the images
    private void uploadImages(){
        Retrofit retrofit = NetworkClient.getRetrofit();
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

        List<MultipartBody.Part> parts = new ArrayList<>();
        HashMap<String, RequestBody> map = new HashMap<>();

        //iterate through the items and extracting the images as well as their attributes to be inserted through the post method
        for (int i = 0; i < itemsList.size(); i++) {
            Items item = itemsList.get(i);
            ImageData imageData = itemsImageData.get(i);
            parts.add(prepareFilePart("uploadedImages", item.getUri()));
            File currentFile = new File(item.getUri().toString().substring(5));
            String partString = imageData.getNormalizedWidth()+"_" + imageData.getNormalizedHeight()+"_" + imageData.getNormalizedCenterX()+"_" + imageData.getNormalizedCenterY();
            RequestBody partBody = createPartFromString(partString);

            map.put(currentFile.getName(), partBody);
        }
        RequestBody nameLabel = createPartFromString(itemsList.get(0).getLabelName());
        map.put("nameLabel",nameLabel);

        // Enqueue the Call to send the post method in another thread and create a callback to get the response back from the server
        // and add the item to the database for future detection
        Call<ResponseBody> call = uploadAPIs.uploadMultipleImages(map, parts);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.errorBody() != null){
                    try {
                        Log.e("OnResponse", response.errorBody().string() );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    final SQLiteOpenHelper sqLiteOpenHelper = new ItemsSQLiteOpenHelper(getApplicationContext());
                    final SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
                    Items itemSelected = itemsList.get(0);
                    ItemsSQLiteOpenHelper.insertItem(db, itemSelected.getName(), -1, itemSelected.getLabelName(), -1, itemSelected.getUri().toString()); //Change the last two arguments
                    Toast.makeText(AddItemActivity.this, "Item uploaded!", Toast.LENGTH_SHORT).show();
                    Intent itemsIntent = new Intent(AddItemActivity.this, ItemsActivity.class);
                    itemsIntent.putExtra("train", true);
                    startActivity(itemsIntent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, t.getMessage(), t );
                Toast.makeText(AddItemActivity.this, "Failed to upload item! Try again", Toast.LENGTH_SHORT).show();
            }
        });


    }

    //This method converts a string to a Retrofit part to be sent later
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    //This method takes a partName and Uri of the image to be uploaded and converts that image into a requestBody from the file.
    //Then, returns a MultiPartBody.Part representing the image with the part name and file name.
    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {

        File file = new File(fileUri.toString().substring(5));
        String fileType = getMimeType(file.toString());

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse(fileType), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    //This method is used to identify a files mime type, it is used in prepareFilePart method
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


    //This method handles the result of activities, in this case, it handles the result taken from the UCrop activity which returns the Uri of the cropped image
    //Then, after that image is received, this method opens another activity (BoundingBoxActivity) and waits for its result which is the bounding box of the item
    //normalized for later use in the training of the YOLO model
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            UCrop.getOutputImageWidth(data);
        }catch (Exception e) {
            if(itemsList.size() == 0){
                final EditText nameEditText = findViewById(R.id.item_name_edit_text);
                TextView textView = findViewById(R.id.add_item_text_view);
                //Re-enabling the edit text so that a change can be done
                nameEditText.setEnabled(true);
                nameEditText.setVisibility(View.VISIBLE);
                textView.setEnabled(false);
                textView.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Process Interrupted", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(this, "Process Interrupted, please add an image", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
            final Uri resultUri = UCrop.getOutput(data);

            coordinates = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() != RESULT_CANCELED) {
                            // Add same code that you want to add in onActivityResult method
                            Intent resultData = result.getData();
                            int randomNumber = new Random().nextInt(10000);
                            @SuppressLint("DefaultLocale") String randomNumberString = String.format("%04d", randomNumber);
                            EditText itemNameEditText = findViewById(R.id.item_name_edit_text);
                            String itemName = itemNameEditText.getText().toString();
                            String nameLabel = itemName + randomNumberString;
                            Items item = new Items(itemName, 0, nameLabel, -1, resultUri);

                            ImageData imageData = new ImageData(resultData.getFloatExtra("normalizedHeight", 0), resultData.getFloatExtra("normalizedWidth", 0)
                                    , resultData.getFloatExtra("normalizedCenterX", 0), resultData.getFloatExtra("normalizedCenterY", 0));
                            itemsImageData.add(imageData);
                            itemsList.add(item);
                            ListView listView = findViewById(R.id.add_item_list_view);
                            AddItemAdapter adapter = new AddItemAdapter(AddItemActivity.this, itemsList, listView, AddItemActivity.this);
                            listView.setAdapter(adapter);
                        }else{
                            Toast.makeText(AddItemActivity.this, "Cancelled!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            final Intent intent = new Intent(AddItemActivity.this, BoundingBoxActivity.class);
            intent.putExtra("path", resultUri.toString());
            coordinates.launch(intent);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Log.e(TAG, cropError.getMessage());
        }
    }
}