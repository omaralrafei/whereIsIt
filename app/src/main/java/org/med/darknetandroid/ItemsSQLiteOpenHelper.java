package org.med.darknetandroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import androidx.annotation.Nullable;

public class ItemsSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "Items_database";
    private static final int DB_VERSION = 1;

    public ItemsSQLiteOpenHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE ITEMS (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "NAME TEXT," +
                "IMAGE_RESOURCE_ID INTEGER," +
                "CLASS_ID INTEGER," +
                "URI TEXT," +
                "LABELNAME TEXT)");


        insertItem(db, "My Remote", R.drawable.remote1,"My Remote5231782Omar7126354", -1, "");
        insertItem(db, "My laptop", R.drawable.laptop1, "My laptop134543Omar12323423", -1, "");
        insertItem(db, "My phone", R.drawable.phone1, "My phone2983647813Omar81637842", -1, "");
        insertItem(db, "My Keys", R.drawable.key1, "My Keys1283741234Omar19827321", -1, "");
        insertItem(db, "laptop", R.drawable.laptop2, "laptop", -1, "");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void insertItem(SQLiteDatabase db, String name, int imageId, String labelName, int classId, String uri){
        ContentValues contentValues = new ContentValues();
        contentValues.put("NAME", name);
        contentValues.put("IMAGE_RESOURCE_ID", imageId);
        contentValues.put("CLASS_ID", classId);
        contentValues.put("LABELNAME", labelName);
        contentValues.put("URI", uri);
        db.insert("ITEMS", null, contentValues);
    }
}
