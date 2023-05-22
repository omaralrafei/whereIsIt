package org.med.darknetandroid;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//This class is a database adapter that converts values in the database to a List of Items
public class DatabaseAdapter {
    SQLiteOpenHelper sqLiteOpenHelper;
    SQLiteDatabase db;
    List<Items> itemsList = new ArrayList<>();
    Context context;
    private static List<String> classNames = new ArrayList<>();

    public DatabaseAdapter(Context context, SQLiteOpenHelper sqLiteOpenHelper, SQLiteDatabase db) {
        this.sqLiteOpenHelper = sqLiteOpenHelper;
        this.db = db;
        this.context = context;
    }

    public List<Items> getAllItems(){
        Cursor cursor = db.query("ITEMS", null, null, null, null, null, null);

        classNames = CameraActivity.readLabels("labels.txt", this.context);
        while(cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("NAME"));
            int image_resource = cursor.getInt(cursor.getColumnIndex("IMAGE_RESOURCE_ID"));
            String uriString = cursor.getString(cursor.getColumnIndex("URI"));
            Uri uri = Uri.EMPTY;
            if(!uriString.equalsIgnoreCase(""))
                uri = Uri.parse(uriString);
            String labelName = cursor.getString(cursor.getColumnIndex("LABELNAME"));
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            int classId=0;
            for (int i = 0; i < classNames.size(); i++) {
                String nameClass = classNames.get(i);
                Log.e("labelName", labelName );
                if (nameClass.equalsIgnoreCase(labelName)) {
                    Log.e("entered", "here" );
                    classId = i;
                }
            }
            if(classId == -1)
                classId = cursor.getInt(cursor.getColumnIndex("CLASS_ID"));

            Items instrument = new Items(id, name, image_resource, labelName, classId, uri);
            itemsList.add(instrument);

        }
        cursor.close();
        return itemsList;
    }
}
