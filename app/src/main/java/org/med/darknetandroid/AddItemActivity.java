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
            parts.add(prepareFilePart("uploadImages", item.getUri()));
            RequestBody normalizedWidth = createPartFromString(String.valueOf(imageData.getNormalizedWidth()));
            RequestBody normalizedHeight = createPartFromString(String.valueOf(imageData.getNormalizedHeight()));
            RequestBody normalizedCenterX = createPartFromString(String.valueOf(imageData.getNormalizedCenterX()));
            RequestBody normalizedCenterY = createPartFromString(String.valueOf(imageData.getNormalizedCenterY()));

            map.put("normalizedHeight", normalizedHeight);
            map.put("normalizedWidth", normalizedWidth);
            map.put("normalizedCenterX", normalizedCenterX);
            map.put("normalizedCenterY", normalizedCenterY);
        }
        RequestBody nameLabel = createPartFromString(itemsList.get(0).getLabelName());
        map.put("nameLabel",nameLabel);

        // Enqueue the Call to send the post method in another thread and create a callback to get the response back from the server
        // and add the item to the database for future detection
        Call<ResponseBody> call = uploadAPIs.uploadMultipleImages(map, parts);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e(TAG, response.message());
                final SQLiteOpenHelper sqLiteOpenHelper = new ItemsSQLiteOpenHelper(getApplicationContext());
                final SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
                Items itemSelected = itemsList.get(0);
                ItemsSQLiteOpenHelper.insertItem(db, itemSelected.getName(), -1, itemSelected.getLabelName(), -1, itemSelected.getUri().toString()); //Change the last two arguments
                Toast.makeText(AddItemActivity.this, "Item uploaded!", Toast.LENGTH_SHORT).show();
                Intent welcome = new Intent(AddItemActivity.this, ItemsActivity.class);
                startActivity(welcome);
                finish();
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

    //This method
    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri

        File file = new File(fileUri.toString().substring(5));
        String fileType = getMimeType(file.toString());

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse(fileType), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            UCrop.getOutputImageWidth(data);
        }catch (Exception e) {
//            Intent intent = new Intent(AddItemActivity.this, WelcomeActivity.class);
//            startActivity(intent);
//            AddItemActivity.this.finish();
//            return;
            Toast.makeText(this, "Process Interrupted, please add an image", Toast.LENGTH_SHORT).show();
        }
        if(requestCode == UCrop.REQUEST_CROP){
            final Uri resultUri = UCrop.getOutput(data);

            coordinates = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
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