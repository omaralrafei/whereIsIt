package org.med.darknetandroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

public class WelcomeActivity extends AppCompatActivity {

    String labelsPath = NetworkClient.baseUrl+ "custom.names";
    String cfgPath = NetworkClient.baseUrl+ "yolov4-tiny-custom.cfg";
    String weightsPath = NetworkClient.baseUrl+ "yolov4-tiny-custom_best.weights";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ImageView splash = findViewById(R.id.welcome_imageView);
        TextView textView = findViewById(R.id.welcome_loading);
        textView.setText("Downloading resources");
        splash.setImageResource(R.drawable.loading);
//        Bundle error = getIntent().getExtras();
//        if(error != null && error.getBoolean("error", false)){
//            textView.setText("Failed to download resources, Check your internet connection");
//            splash.setImageResource(R.drawable.error);
//        }else{
//          }
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
            //new DownloadFileFromURL(this).execute(weightsPath);
            //new DownloadFileFromURL(this).execute(cfgPath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 1){
            if(grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED && requestCode == 0) {
                new DownloadFileFromURL(this).execute(labelsPath);
                //new DownloadFileFromURL(this).execute(weightsPath);
                //new DownloadFileFromURL(this).execute(cfgPath);
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

    public class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @SuppressLint("StaticFieldLeak")
        Activity activity;
        public DownloadFileFromURL(Activity activity){
            this.activity = activity;
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            String fileName = f_url[0].split("/")[3];
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                Log.e("hang", "doInBackground: ");
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lengthOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                OutputStream output = new FileOutputStream(getFilesDir().toString()
                        + "/" +fileName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

                if(fileName.equalsIgnoreCase("custom.names") && activity.getClass().getSimpleName().equalsIgnoreCase("WelcomeActivity")){
                    Intent welcome = new Intent(activity, ItemsActivity.class);
                    activity.startActivity(welcome);
                    activity.finish();
                }
                Log.i("upload", "1 File Uploaded");

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                if(fileName.equalsIgnoreCase("custom.names")) {
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