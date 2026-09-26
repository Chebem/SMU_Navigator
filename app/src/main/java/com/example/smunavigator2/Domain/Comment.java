package com.example.smunavigator2.Domain;

// Stored at profiles/{postOwner}/posts/{postId}/comments/{pushId}
public class Comment {
    public String userId;
    public String text;
    public long timestamp;

    public Comment() {
        // Default constructor required for Firebase
    }

    public Comment(String userId, String text, long timestamp) {
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
    }
}
