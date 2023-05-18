package org.med.darknetandroid;

import android.net.Uri;

public class Items {
    private String name;
    private int imageResource;
    private String labelName;
    private int id;
    private int classId;
    private Uri uri;

    public Items(int id, String name, int imageResource, String labelName, int classId, Uri uri) {
        this.id = id;
        this.name = name;
        this.imageResource = imageResource;
        this.labelName = labelName;
        this.classId = classId;
        this.uri = uri;
    }

    public Items(String name, int imageResource, String labelName, int classId, Uri uri) {
        this.name = name;
        this.imageResource = imageResource;
        this.labelName = labelName;
        this.classId = classId;
        this.uri = uri;
    }

    public Uri getUri(){return uri;}

    public String getLabelName() {
        return labelName;
    }

    public String getName() {
        return name;
    }

    public int getClassId(){return classId;}

    public int getId() {
        return id;
    }

    public int getImageResource() {
        return imageResource;
    }
}
