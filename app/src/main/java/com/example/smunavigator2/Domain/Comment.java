package com.example.smunavigator2.Domain;

import com.google.firebase.database.Exclude;

// Stored at profiles/{postOwner}/posts/{postId}/comments/{pushId}
public class Comment {
    public String userId;
    public String text;
    public long timestamp;

    @Exclude
    public String key; // push id, set after reading

    public Comment() {
        // Default constructor required for Firebase
    }

    public Comment(String userId, String text, long timestamp) {
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
    }
}
