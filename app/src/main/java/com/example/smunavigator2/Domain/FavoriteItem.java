package com.example.smunavigator2.Domain;

public class FavoriteItem {
    private String name;
    private String category;
    private double distance; // in km
    private String imageUrl;
    private int favoriteCount;
    private double lat;
    private double lng;

    public FavoriteItem() {
        // Required for DataSnapshot.getValue(FavoriteItem.class)
    }

    public FavoriteItem(String name, String category, double distance, String imageUrl, int favoriteCount, double lat, double lng) {
        this.name = name;
        this.category = category;
        this.distance = distance;
        this.imageUrl = imageUrl;
        this.favoriteCount = favoriteCount;
        this.lat = lat;
        this.lng = lng;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getDistance() {
        return distance;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}