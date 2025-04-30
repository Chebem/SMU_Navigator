package com.example.smunavigator2.Domain;


import java.io.Serializable;

public class ItemDomain implements Serializable
{
    private String id;               // Unique ID (optional if Firebase key is used)
    private String name;             // Name of the facility
    private String description;      // Short info about the building and its purpose
    private String imagePath;        // URL from Firebase Storage
    private String openingHours;     // E.g. "08:00 - 22:00"
    private String contactNumber;    // E.g. "+82 43-649-1234"
    private String locationDetails;  // Extra info like floor, block, directions
    private double latitude;         // GPS location
    private double longitude;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getLocationDetails() {
        return locationDetails;
    }

    public void setLocationDetails(String locationDetails) {
        this.locationDetails = locationDetails;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}





