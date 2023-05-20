package org.med.darknetandroid;

public class ImageData {
    int xBegin;
    int yBegin;
    int xEnd;
    int yEnd;
    int height;
    int width;


    public ImageData(int xBegin, int yBegin, int xEnd, int yEnd, int height, int width) {
        this.xBegin = xBegin;
        this.yBegin = yBegin;
        this.xEnd = xEnd;
        this.yEnd = yEnd;
        this.height = height;
        this.width = width;
    }

    public void setxBegin(int xBegin) {
        this.xBegin = xBegin;
    }

    public void setyBegin(int yBegin) {
        this.yBegin = yBegin;
    }

    public void setxEnd(int xEnd) {
        this.xEnd = xEnd;
    }

    public void setyEnd(int yEnd) {
        this.yEnd = yEnd;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getxBegin() {
        return xBegin;
    }

    public int getyBegin() {
        return yBegin;
    }

    public int getxEnd() {
        return xEnd;
    }

    public int getyEnd() {
        return yEnd;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
