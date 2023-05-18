package org.med.darknetandroid;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

public class AddItemActivity extends AppCompatActivity {

    ActivityResultLauncher<String> mGetContent;
    List<Items> itemsList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        final SQLiteOpenHelper sqLiteOpenHelper = new ItemsSQLiteOpenHelper(this);
        final SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
        Button addItemButton = findViewById(R.id.add_item_add_button);
        Button submitButton = findViewById(R.id.add_item_submit_button);
        Button cancelButton = findViewById(R.id.add_item_cancel_button);
        final EditText nameEditText = findViewById(R.id.item_name_edit_text);

        final String name = nameEditText.getText().toString();
        int randomNumber = new Random().nextInt(10000);
        @SuppressLint("DefaultLocale") String randomNumberString = String.format("%04d", randomNumber);
        final String nameLabel = name + randomNumberString;
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
                    //checkPermissions(3);

                }
            }
        });
        mGetContent=registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                String dest_uri = new StringBuilder(UUID.randomUUID().toString()).append(".png").toString();
                UCrop.of(result, Uri.fromFile(new File(getExternalMediaDirs()[0],dest_uri))).withAspectRatio(1,1).start(AddItemActivity.this);
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemsList.size() > 0){
                    Items itemSelected = itemsList.get(0);
                    ItemsSQLiteOpenHelper.insertItem(db, itemSelected.getName(), -1, itemSelected.getLabelName(), -1, itemSelected.getUri().toString()); //Change the last two arguments
                    Intent welcome = new Intent(AddItemActivity.this, WelcomeActivity.class);
                    startActivity(welcome);
                    finish();
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