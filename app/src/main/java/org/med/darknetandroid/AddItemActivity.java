package org.med.darknetandroid;

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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
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

    ActivityResultLauncher<String> mGetContent;
    List<Items> itemsList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Button addItemButton = findViewById(R.id.add_item_add_button);
        Button submitButton = findViewById(R.id.add_item_submit_button);
        Button cancelButton = findViewById(R.id.add_item_cancel_button);
        final EditText nameEditText = findViewById(R.id.item_name_edit_text);

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
                    mGetContent.launch("image/*");
                }
            }
        });
        mGetContent=registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                String dest_uri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
                UCrop.of(result, Uri.fromFile(new File(getExternalMediaDirs()[0],dest_uri))).withAspectRatio(1,1).start(AddItemActivity.this);
            }
        });
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
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent welcome = new Intent(AddItemActivity.this, WelcomeActivity.class);
                startActivity(welcome);
                finish();
            }
        });
    }


    private void uploadImages(){

        Retrofit retrofit = NetworkClient.getRetrofit();
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
        // ... possibly many more file uris

        // create list of file parts (photo, video, ...)
        List<MultipartBody.Part> parts = new ArrayList<>();
        HashMap<String, RequestBody> map = new HashMap<>();

        for (int i = 0; i < itemsList.size(); i++) {
            Items item = itemsList.get(i);
            parts.add(prepareFilePart("uploadImages", item.getUri()));
            RequestBody nameLabel = createPartFromString(item.getLabelName());
            map.put("nameLabel",nameLabel);
        }
        // finally, execute the request
        Call<ResponseBody> call = uploadAPIs.uploadMultipleImages(map, parts);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                final SQLiteOpenHelper sqLiteOpenHelper = new ItemsSQLiteOpenHelper(getApplicationContext());
                final SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
                Items itemSelected = itemsList.get(0);
                ItemsSQLiteOpenHelper.insertItem(db, itemSelected.getName(), -1, itemSelected.getLabelName(), -1, itemSelected.getUri().toString()); //Change the last two arguments
                Toast.makeText(AddItemActivity.this, "Item uploaded!", Toast.LENGTH_SHORT).show();
                Intent welcome = new Intent(AddItemActivity.this, WelcomeActivity.class);
                startActivity(welcome);
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(AddItemActivity.this, "Failed to upload item! Try again", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri

        File file = new File(fileUri.toString());

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UCrop.REQUEST_CROP){
            final Uri resultUri = UCrop.getOutput(data);

            int randomNumber = new Random().nextInt(10000);
            @SuppressLint("DefaultLocale") String randomNumberString = String.format("%04d", randomNumber);
            EditText itemNameEditText = findViewById(R.id.item_name_edit_text);
            String itemName = itemNameEditText.getText().toString();
            String nameLabel = itemName + randomNumberString;
            Items item = new Items(itemName, 0, nameLabel, -1, resultUri);
            itemsList.add(item);

            ListView listView = findViewById(R.id.add_item_list_view);
            AddItemAdapter adapter = new AddItemAdapter(this, itemsList, listView, this);
            listView.setAdapter(adapter);

        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Log.e(TAG, cropError.getMessage());
        }
        if (requestCode==3)
        {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            File destination = new File(this.getExternalCacheDir(),"temp.jpg");
            new AndroidBmpUtil().save(thumbnail, destination.toString());
            String dest_uri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
            Uri destinationFileUri = Uri.fromFile(new File(getCacheDir(),dest_uri));
            UCrop.of(Uri.fromFile(destination), destinationFileUri).withAspectRatio(1,1).start(this);
        }
    }
}