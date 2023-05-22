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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

// This activity is the one where the resources are downloaded as well as the welcoming activity
public class WelcomeActivity extends AppCompatActivity {

    String labelsPath = NetworkClient.baseUrl+ "labels";

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

    //Checking write file permissions and camera access
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

    //when the permissions are returned
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == RESULT_OK && grantResults[1] == RESULT_OK){
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

    // this class is made to download files such as the model config and weights file
    @SuppressLint("StaticFieldLeak")
    public static class DownloadFileFromURL extends AsyncTask<String, String, String> {

        String labelsPath = NetworkClient.baseUrl+ "labels.txt";
        String cfgPath = NetworkClient.baseUrl+ "yolov3-tiny.cfg";
        String weightsPath = NetworkClient.baseUrl+ "yolov3-tiny_best.weights";
        Activity activity;
        public DownloadFileFromURL(Activity activity){
            this.activity = activity;
        }

        //This is the method that is called when downloading resources
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

            String outputName = activity.getFilesDir().toString() + "/" + fileName;
            Log.e("File Name: ", outputName);
            OutputStream output = new FileOutputStream(outputName);

            int count;
            byte[] data = new byte[8192];

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        }

        //download file asynchronously (model config, model weights, and the labels
        @Override
        protected String doInBackground(String... f_url) {
            try{

                downloadFile(labelsPath);
                Log.i("download", "1 File Downloaded");
                downloadFile(cfgPath);
                Log.i("download", "2 Files Downloaded");
                downloadFile(weightsPath);

                if(activity.getClass().getSimpleName().equalsIgnoreCase("WelcomeActivity")){
                    Intent welcome = new Intent(activity, ItemsActivity.class);
                    activity.startActivity(welcome);
                    activity.finish();
                }else if(activity.getClass().getSimpleName().equalsIgnoreCase("AddItemActivity")){
                    SQLiteOpenHelper sqLiteOpenHelper = new ItemsSQLiteOpenHelper(activity);
                    SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
                    DatabaseAdapter databaseAdapter = new DatabaseAdapter(activity, sqLiteOpenHelper, db);
                    List<Items> itemsList = databaseAdapter.getAllItems();
                    ListView listView = activity.findViewById(R.id.items_listView);

                    MyAdapter adapter = new MyAdapter(activity, itemsList, listView, activity);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener((AdapterView.OnItemClickListener) activity);
                }
                Log.i("download", "3 Files Downloaded");

            } catch ( IOException e) {
                e.printStackTrace();
                Intent welcome = new Intent(activity, ErrorActivity.class);
                welcome.putExtra("error", true);
                activity.startActivity(welcome);
                activity.finish();
            }
            return null;
        }
    }
}