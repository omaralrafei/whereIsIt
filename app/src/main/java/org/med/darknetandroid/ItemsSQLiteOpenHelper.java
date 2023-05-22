package org.med.darknetandroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import androidx.annotation.Nullable;

//This class handles the creation of the database and the insertion called by other classes
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
