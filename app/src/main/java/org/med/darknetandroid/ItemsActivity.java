package org.med.darknetandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

//This is the activity where the user's items are displayed and where they can select an item for detection
public class ItemsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    List<Items> itemsList = new ArrayList<>();
    private static String selectedItem = "";
    static Handler toastHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        ListView listView = findViewById(R.id.items_listView);
        Button addItem = findViewById(R.id.add_item);
        final Button trainButton = findViewById(R.id.items_train_button);
        Button refreshButton = findViewById(R.id.items_refresh_button);

        SQLiteOpenHelper sqLiteOpenHelper = new ItemsSQLiteOpenHelper(this);
        SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(this, sqLiteOpenHelper, db);
        itemsList = databaseAdapter.getAllItems();

        MyAdapter adapter = new MyAdapter(this, itemsList, listView, this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new WelcomeActivity.DownloadFileFromURL(ItemsActivity.this).execute();
            }
        });

        trainButton.setVisibility(View.INVISIBLE);
        trainButton.setEnabled(false);

        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent welcome = new Intent(ItemsActivity.this, AddItemActivity.class);
                startActivity(welcome);
                finish();
            }
        });

        if(getIntent().getBooleanExtra("train", false)){
            trainButton.setVisibility(View.VISIBLE);
            trainButton.setEnabled(true);
        }

        toastHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if(message.what == 0){
                    Toast.makeText(ItemsActivity.this, "Training the model", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ItemsActivity.this, "Sorry, model is currently training", Toast.LENGTH_SHORT).show();
                }
            }
        };

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trainButton.setClickable(false);
                trainButton.setEnabled(false);
                new PostTrain(ItemsActivity.this).execute(NetworkClient.baseUrl + "/train");
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Items item = itemsList.get(position);
        selectedItem = item.getLabelName();
        setContentView(R.layout.activity_welcome);

        int SPLASH_TIME = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent welcome = new Intent(ItemsActivity.this, CameraActivity.class);
                Bundle b = new Bundle();
                b.putString("labelName", selectedItem);
                welcome.putExtras(b);
                startActivity(welcome);
                finish();
            }
        },SPLASH_TIME);
    }

    public static class PostTrain extends AsyncTask<String, String, String> {
        Activity activity;
        public PostTrain(Activity activity){
            this.activity = activity;
        }

        public boolean trainCheck(String fileUrl) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            RequestBody body = RequestBody.create(null, new byte[0]);
            Request request = new Request.Builder().method("POST", body)
                    .url(fileUrl)
                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                String responseMessage = response.message();
                return responseMessage.equalsIgnoreCase("Your objects are being trained");
            }catch (IOException e){
                e.printStackTrace();
                return false;
            }
        }


        @Override
        protected String doInBackground(String... f_url) {
            if(trainCheck(f_url[0])){
                toastHandler.obtainMessage(0);
            }else {
                toastHandler.obtainMessage(1);
            }
            return null;
        }
    }
}