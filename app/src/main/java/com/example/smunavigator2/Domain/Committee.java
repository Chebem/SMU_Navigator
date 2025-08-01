package com.example.smunavigator2.Domain;

import java.io.Serializable;

public class Committee implements Mappable, Serializable {

    private String title;
    private String imageUrl;
    private String location;
    private String description;
    private String openingHours;
    private float score;
    private double latitude;
    private double longitude;

    private String Category;

    @Override
    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    // 🔧 Required empty constructor for Firebase
    public Committee() {}

    // ✅ All-fields constructor (excluding lat/lng, which are set separately)
    public Committee(String title, String imageUrl, String location, String openingHours, float score) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.location = location;
        this.openingHours = openingHours;
        this.score = score;
    }

    // ✅ Getters
    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public float getScore() {
        return score;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    // ✅ Mappable interface implementation
    @Override
    public double getLat() {
        return latitude;
    }

    @Override
    public double getLng() {
        return longitude;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public String getType() {
        return "Committee";
    }

    @Override
    public String getImagePath() {
        return imageUrl;
    }

    // ✅ Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}