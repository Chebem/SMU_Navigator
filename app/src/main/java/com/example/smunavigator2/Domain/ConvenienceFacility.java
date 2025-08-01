package com.example.smunavigator2.Domain;

import java.io.Serializable;

public class ConvenienceFacility implements Mappable, Serializable {
    private String id; // Firebase key (e.g., "CULibrary")
    private String name;
    private String description;
    private String location;
    private String operating_hours;
    private String type;
    private String imagePath;
    private String category;


    private String score;
    private double latitude; // Add latitude field
    private double longitude; // Add longitude field

    public ConvenienceFacility() {
        // Required empty constructor for Firebase
    }

    @Override
    public double getLat() {
        return latitude;
    }

    @Override
    public double getLng() {
        return longitude;
    }

    // ✅ Getter and Setter for ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // ✅ Standard getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOperating_hours() {
        return operating_hours;
    }

    public void setOperating_hours(String operating_hours) {
        this.operating_hours = operating_hours;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
