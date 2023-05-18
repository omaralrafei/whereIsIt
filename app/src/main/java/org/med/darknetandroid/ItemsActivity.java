package org.med.darknetandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

public class ItemsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    List<Items> itemsList = new ArrayList<>();
    private static boolean permissionGranted = false;
    private static String selectedItem = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        ListView listView = findViewById(R.id.items_listView);
        Button addItem = findViewById(R.id.add_item);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions(2);
            }
        });

        SQLiteOpenHelper sqLiteOpenHelper = new ItemsSQLiteOpenHelper(this);
        SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(this, sqLiteOpenHelper, db);
        itemsList = databaseAdapter.getAllItems();

        MyAdapter adapter = new MyAdapter(this, itemsList, listView, this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Items item = itemsList.get(position);
        selectedItem = item.getLabelName();

        if (!permissionGranted) {
            checkPermissions(1);
        }
    }
    public boolean checkPermissions(int requestCode){
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
            return false;
        } else if (requestCode == 2){
            Intent welcome = new Intent(this, AddItemActivity.class);
            startActivity(welcome);
            finish();
        }
            else if (requestCode == 1){
            permissionGranted = true;
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
            return true;
        }
            return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED && requestCode == 1) {
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
        } else if(grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED && requestCode == 2){
            Intent welcome = new Intent(this, AddItemActivity.class);
            startActivity(welcome);
            finish();
        }
        else
        {
            Toast.makeText(this, "Permissions are needed to detect your item", Toast.LENGTH_SHORT).show();
            Intent welcome = new Intent(this, WelcomeActivity.class);
            startActivity(welcome);
            finish();
        }
    }
}