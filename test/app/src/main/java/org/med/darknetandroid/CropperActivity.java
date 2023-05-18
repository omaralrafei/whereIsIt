package org.med.darknetandroid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;

public class CropperActivity extends AppCompatActivity {

    String result;
    Uri fileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);

        readIntent();
        String dest_uri = new StringBuilder(UUID.randomUUID().toString()).append(".png").toString();
        UCrop.of(fileUri, Uri.fromFile(new File(getCacheDir(),dest_uri))).withAspectRatio(1,1).start(CropperActivity.this);
    }

    private void readIntent(){
        Intent intent = getIntent();
        if(intent.getExtras()!= null){
            result= intent.getStringExtra("DATA");
            fileUri = Uri.parse(result);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            ImageView imageView = findViewById(R.id.cropper_imageview);
            imageView.setImageURI(resultUri);
//            Intent returnIntent = new Intent();
//            returnIntent.putExtra("RESULT", resultUri+"");
//            setResult(-1, returnIntent);
//            finish();
        }else if(requestCode==UCrop.RESULT_ERROR){
            final Throwable cropError = UCrop.getError(data);
        }
    }
}