package org.med.darknetandroid;

public class ImageData {
    float normalizedHeight;
    float normalizedWidth;
    float normalizedCenterX;
    float normalizedCenterY;

    public ImageData(float normalizedHeight, float normalizedWidth, float normalizedCenterX, float normalizedCenterY) {
        this.normalizedHeight = normalizedHeight;
        this.normalizedWidth = normalizedWidth;
        this.normalizedCenterX = normalizedCenterX;
        this.normalizedCenterY = normalizedCenterY;
    }

    public float getNormalizedHeight() {
        return normalizedHeight;
    }

    public float getNormalizedWidth() {
        return normalizedWidth;
    }

    public float getNormalizedCenterX() {
        return normalizedCenterX;
    }

    public float getNormalizedCenterY() {
        return normalizedCenterY;
    }

    public void setNormalizedHeight(float normalizedHeight) {
        this.normalizedHeight = normalizedHeight;
    }

    public void setNormalizedWidth(float normalizedWidth) {
        this.normalizedWidth = normalizedWidth;
    }

    public void setNormalizedCenterX(float normalizedCenterX) {
        this.normalizedCenterX = normalizedCenterX;
    }

    public void setNormalizedCenterY(float normalizedCenterY) {
        this.normalizedCenterY = normalizedCenterY;
    }
}
