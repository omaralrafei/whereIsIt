package org.med.darknetandroid;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

public class WelcomeActivity extends AppCompatActivity {

    String labelsPath = NetworkClient.baseUrl+ "labels";
    String cfgPath = NetworkClient.baseUrl+ "cfg";
    String weightsPath = NetworkClient.baseUrl+ "weights";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

    public void checkPermissions(int requestCode){
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int permissionCheckStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);


        if (permissionCheck != PackageManager.PERMISSION_GRANTED || permissionCheckStorage!= PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        } else if (requestCode == 0){
            new DownloadFileFromURL(this).execute(labelsPath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 1){
            if(grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED && requestCode == 0) {
                new DownloadFileFromURL(this).execute(labelsPath);
            }
        }

        else
        {
            Toast.makeText(this, "Permissions are needed to detect your item", Toast.LENGTH_SHORT).show();
            Intent welcome = new Intent(this, WelcomeActivity.class);
            startActivity(welcome);
            finish();
        }
    }



    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 1) {
            moveTaskToBack(false);
        }
        else {
            super.onBackPressed();
        }
    }


    /**
     * Background Async Task to download file
     * */

    @SuppressLint("StaticFieldLeak")
    public class DownloadFileFromURL extends AsyncTask<String, String, String> {

        Activity activity;
        int total = 0;
        public DownloadFileFromURL(Activity activity){
            this.activity = activity;
        }

        public void downloadFile(String fileUrl) throws IOException {
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            Request request = new Request.Builder()
                    .url(fileUrl)
                    .build();
            Log.e("download file", "after builder");

            String fileName = fileUrl.split("/")[3];
            Response response = okHttpClient.newCall(request).execute();

            InputStream is = response.body().byteStream();

            BufferedInputStream input = new BufferedInputStream(is);
            OutputStream output = new FileOutputStream(getFilesDir().toString() + "/" + fileName +".txt");

            int count;
            byte[] data = new byte[1024];

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {

            String fileName = f_url[0].split("/")[3];
            try{

                downloadFile(f_url[0]);
                Log.i("upload", "1 File Uploaded");
                downloadFile(weightsPath);
                Log.i("upload", "2 Files Uploaded");
                downloadFile(cfgPath);
                if(fileName.equalsIgnoreCase("labels") && activity.getClass().getSimpleName().equalsIgnoreCase("WelcomeActivity")){
                    Intent welcome = new Intent(activity, ItemsActivity.class);
                    activity.startActivity(welcome);
                    activity.finish();
                }
                Log.i("upload", "3 Files Uploaded");

            } catch ( IOException e) {
                e.printStackTrace();
                Log.e("Error", "doInBackground: ioexception");
                if(fileName.equalsIgnoreCase("labels")) {
                    Intent welcome = new Intent(activity, ErrorActivity.class);
                    welcome.putExtra("error", true);
                    activity.startActivity(welcome);
                    activity.finish();
                }
            }
            return null;
        }
    }
}