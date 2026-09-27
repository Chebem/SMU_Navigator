package com.example.smunavigator2.Domain;

import com.google.firebase.database.Exclude;

// Stored at notifications/{uid}/{key}; written by the Cloud Function, marked read by the app
public class AppNotification {
    public String type;      // "follow"
    public String fromUid;
    public long timestamp;
    public boolean read;

    @Exclude
    public String key;

    public AppNotification() {
        // Default constructor required for Firebase
    }
}
