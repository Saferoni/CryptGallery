package com.safercrypt.scgallery;

import android.graphics.Bitmap;

public class ImageItem {
    private Bitmap image;
    private String path;
    private String pathPreview;

    public ImageItem(Bitmap image, String path, String pathPreview) {
        super();
        this.image = image;
        this.path = path;
        this.pathPreview = pathPreview;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathPreview(){
        return pathPreview;
    }

    public void setPathPreview(String pathPreview) {
        this.pathPreview = pathPreview;
    }
}
