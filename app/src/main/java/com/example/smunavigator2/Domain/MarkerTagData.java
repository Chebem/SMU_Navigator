// MarkerTagData.java
package com.example.smunavigator2.Domain;

public class MarkerTagData {
    public String name;
    public String address;
    public String hours;
    public String description;
    public String imageUrl;
    public String category;
    public double lat;
    public double lng;
    public String floors;
    public String supervisors;
    public String phoneNumber; // ✅ NEW FIELD

    // ✅ Constructor for dorms
    public MarkerTagData(String name, String address, String hours, String description, String imageUrl, String category, String floors, String supervisors, double lat, double lng) {
        this.name = name;
        this.address = address;
        this.hours = hours;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.floors = floors;
        this.supervisors = supervisors;
        this.phoneNumber = null;
        this.lat = lat;
        this.lng = lng;
    }

    // ✅ Constructor for stores with optional phoneNumber
    public MarkerTagData(String name, String address, String hours, String description, String imageUrl, String category, String phoneNumber, double lat, double lng) {
        this.name = name;
        this.address = address;
        this.hours = hours;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.phoneNumber = phoneNumber;
        this.floors = null;
        this.supervisors = null;
        this.lat = lat;
        this.lng = lng;
    }
}