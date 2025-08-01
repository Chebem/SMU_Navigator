package com.example.smunavigator2.Domain;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class PlaceClusterItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final String category; // Optional for marker icon logic
    private final String imageUrl; // Optional for showing in custom cluster view

    public PlaceClusterItem(double lat, double lng, String title, String snippet) {
        this(lat, lng, title, snippet, null, null);
    }

    public PlaceClusterItem(double lat, double lng, String title, String snippet, String category, String imageUrl) {
        this.position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}