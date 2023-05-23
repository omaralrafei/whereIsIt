package org.med.darknetandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

// This activity is the one where the resources are downloaded as well as the welcoming activity
public class WelcomeActivity extends AppCompatActivity {

    public static String cfgName = "yolov3-tiny.cfg";
    public static String labelsName = "labels.txt";
    public static String weightsName = "yolov3-tiny_best.weights";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ImageView splash = findViewById(R.id.welcome_imageView);
        TextView textView = findViewById(R.id.welcome_loading);
        textView.setText("Downloading resources");
        splash.setImageResource(R.drawable.loading);

        checkPermissions(0);
    }

    //Checking write file permissions and camera access
    public void checkPermissions(int requestCode){
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int permissionCheckStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED || permissionCheckStorage!= PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        } else if (requestCode == 0){
            //If the activity was launched to re-download resources, call downloadFileFromUrl
            Bundle bundle = getIntent().getExtras();
            if(bundle != null) {
                if (bundle.getBoolean("refresh", false)) {
                    new DownloadFileFromURL(this).execute();
                }
                else{
                    Intent welcome = new Intent(this, ItemsActivity.class);
                    startActivity(welcome);
                    finish();
                }
            }else {
                Intent welcome = new Intent(this, ItemsActivity.class);
                startActivity(welcome);
                finish();
            }
        }
    }

    //when the permissions are returned, download the resources
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED && requestCode == 0) {
            new DownloadFileFromURL(this).execute();
        }
        else
        {
            Toast.makeText(this, "Permissions are needed to detect your item", Toast.LENGTH_SHORT).show();
            Intent welcome = new Intent(this, WelcomeActivity.class);
            startActivity(welcome);
            finish();
        }
    }

    // this class is made to download files such as the model config and weights file
    public static class DownloadFileFromURL extends AsyncTask<String, String, String> {

        String labelsPath = NetworkClient.baseUrl+ labelsName;
        String cfgPath = NetworkClient.baseUrl+ cfgName;
        String weightsPath = NetworkClient.baseUrl+ weightsName;
        @SuppressLint("StaticFieldLeak")
        Activity activity;
        public DownloadFileFromURL(Activity activity){
            this.activity = activity;
        }

        //This is the method that is called when downloading resources
        public void downloadFile(String fileUrl) throws IOException {

            //Create a new okHttpClient and read the contents using a buffer of size 1 KB and store them in files directory
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            Request request = new Request.Builder()
                    .url(fileUrl)
                    .build();

            String fileName = fileUrl.split("/")[3];
            Response response = okHttpClient.newCall(request).execute();

            ResponseBody rb = response.body();
            InputStream is;
            is = rb.byteStream();

            BufferedInputStream input = new BufferedInputStream(is);

            String outputName = activity.getFilesDir() + "/" + fileName;
            FileOutputStream output = new FileOutputStream(outputName);

            int count;
            byte[] data = new byte[1024];

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        }

        //download files (model config, model weights, and the labels)
        @Override
        protected String doInBackground(String... f_url) {
            try{
                downloadFile(labelsPath);
                Log.i("download", "1 File Downloaded");
                downloadFile(cfgPath);
                Log.i("download", "2 Files Downloaded");
                downloadFile(weightsPath);
                Log.i("download", "3 Files Downloaded");

                Intent welcome = new Intent(activity, ItemsActivity.class);
                activity.startActivity(welcome);
                activity.finish();
            } catch ( IOException e) {
                e.printStackTrace();
                Intent error = new Intent(activity, ErrorActivity.class);
                error.putExtra("error", true);
                activity.startActivity(error);
                activity.finish();
            }
            return null;
        }
    }
}