package com.example.smunavigator2.Domain;

// Stored at reviews/{placeKey}/{userId}: one review per person per place (writing again updates it)
public class Review {
    public String userId;
    public int rating;      // 1-5 stars
    public String text;     // optional, up to 500 characters
    public long timestamp;

    public Review() {
        // Default constructor required for Firebase
    }

    public Review(String userId, int rating, String text, long timestamp) {
        this.userId = userId;
        this.rating = rating;
        this.text = text;
        this.timestamp = timestamp;
    }
}
