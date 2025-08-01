package com.example.smunavigator2.Domain;

import java.util.List;

public class Post {
    private List<String> imageUrls;

    public String mainImage;
    private String caption;
    private String userId; // 🔑 Important for user profile lookup
    private long timestamp; // Optional for sorting by date

    public Post() {
        // Default constructor required for Firebase
    }

    private String visibility;

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Post(List<String> imageUrls, String caption, String userId, long timestamp) {
        this.imageUrls = imageUrls;
        this.caption = caption;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

}