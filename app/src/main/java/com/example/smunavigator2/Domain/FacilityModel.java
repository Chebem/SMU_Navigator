package com.example.smunavigator2.Domain;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.Map;

public class FacilityModel implements Mappable, Serializable {
    private Map<String, String> name; // Multilingual name map (e.g., {"en": "Library", "ko": "도서관"})
    private String nameString;        // Fallback for single-language name from Firebase
    private String location;          // Display location or building name
    private String description;
    private String imagePath;
    private double lat;
    private double lng;
    private String id;                // Firebase key
    private String category;
    private String type;
    private String operating_hours;

    public FacilityModel() {
        // Default constructor required for Firebase
    }

    public FacilityModel(Map<String, String> name, String location, String description, String imagePath,
                         double lat, double lng, String id, String category, String type, String operating_hours) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.imagePath = imagePath;
        this.lat = lat;
        this.lng = lng;
        this.id = id;
        this.category = category;
        this.type = type;
        this.operating_hours = operating_hours;
    }

    @PropertyName("name")
    public void setNameString(String nameString) {
        this.nameString = nameString;
        if (this.name == null) {
            this.name = Map.of("en", nameString); // fallback map if no full name map exists
        }
    }

    @PropertyName("name")
    public String getNameString() {
        return nameString;
    }

    public Map<String, String> getNameMap() {
        return name;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public String getLocation() {
        return location != null ? location : "";
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath != null ? imagePath : "";
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @PropertyName("latitude")
    public double getLat() {
        return lat;
    }

    @PropertyName("latitude")
    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category != null ? category : "";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type != null ? type : "Facility";
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperatingHours() {
        return operating_hours != null ? operating_hours : "Hours Unknown";
    }

    public void setOperatingHours(String operating_hours) {
        this.operating_hours = operating_hours;
    }

    // Mappable interface implementations
    @Override
    public String getName() {
        if (name != null && name.containsKey("en")) {
            return name.get("en");
        }
        return nameString != null ? nameString : "Facility";
    }

    public String getImage() {
        return getImagePath();
    }
}
