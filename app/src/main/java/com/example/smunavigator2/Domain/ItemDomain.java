package com.example.smunavigator2.Domain;

import java.io.Serializable;

public class ItemDomain implements Mappable, Serializable {

    private String id;
    private String floors;
    private String gender;



    private String total_rooms;
    private String building;
    private int capacity;
    private String supervisors;
    private String name;
    private String description;
    private String imagePath;
    private String openingHours;
    private String contactNumber;
    private String locationDetails;
    private String category;


    private double latitude;
    private double longitude;
    private String Score;

    // Required empty constructor for Firebase
    public ItemDomain() {}

    // Getters and Setters

    public String getFloors() {
        return floors;
    }

    public void setFloors(String floors) {
        this.floors = floors;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getTotal_rooms() {
        return total_rooms;
    }

    public void setTotal_rooms(String total_rooms) {
        this.total_rooms = total_rooms;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getSupervisors() {
        return supervisors;
    }

    public void setSupervisors(String supervisors) {
        this.supervisors = supervisors;
    }
    public String getScore() {
        return Score;
    }

    public void setScore(String score) {
        Score = score;
    }

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

    // ✅ Mappable interface methods
    @Override
    public double getLat() {
        return latitude;
    }

    @Override
    public double getLng() {
        return longitude;
    }

    @Override
    public String getType() {
        return "Facility";
    }
}