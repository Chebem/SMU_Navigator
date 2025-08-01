package com.example.smunavigator2.Domain;

import java.io.Serializable;
import java.util.List;

public class StoreModel implements Serializable {

    private String name;
    private String activity;
    private String address;
    private String shortAddress;
    private String phone_number;
    private List<String> opening_hours;
    private String imagePath;
    private double latitude;
    private double longitude;
    private String category;

    // Required empty constructor for Firebase
    public StoreModel() {}

    // Updated constructor with List<String> for opening_hours
    public StoreModel(String name, String activity, String address, String shortAddress,
                      String phone_number, List<String> opening_hours, String imagePath,
                      double latitude, double longitude, String category) {
        this.name = name;
        this.activity = activity;
        this.address = address;
        this.shortAddress = shortAddress;
        this.phone_number = phone_number;
        this.opening_hours = opening_hours;
        this.imagePath = imagePath;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getActivity() {
        return activity;
    }

    public String getAddress() {
        return address;
    }

    public String getShortAddress() {
        return shortAddress;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public List<String> getOpening_hours() {
        return opening_hours;
    }

    public String getImagePath() {
        return imagePath;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCategory() {
        return category;
    }

    // Firebase alias methods for 'lat' and 'lng' keys
    public void setLat(double lat) {
        this.latitude = lat;
    }

    public double getLat() {
        return this.latitude;
    }

    public void setLng(double lng) {
        this.longitude = lng;
    }

    public double getLng() {
        return this.longitude;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setShortAddress(String shortAddress) {
        this.shortAddress = shortAddress;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public void setOpening_hours(List<String> opening_hours) {
        this.opening_hours = opening_hours;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}