package com.example.imageslideralt.Resouces;

public class ImageEntity {
    private String ImageUrl;

    public ImageEntity() {
    }

    public ImageEntity(String imageURL) {
        this.ImageUrl = imageURL;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.ImageUrl = imageUrl;
    }
}
