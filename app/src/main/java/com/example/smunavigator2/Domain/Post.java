package com.example.smunavigator2.Domain;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Post {
    private List<String> imageUrls;

    public String mainImage;
    private String caption;
    private String userId; //  Important for user profile lookup
    private long timestamp; // Optional for sorting by date

    public Post() {
        // Default constructor required for Firebase
    }

    private String visibility;

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Post(List<String> imageUrls, String caption, String userId, long timestamp) {
        this.imageUrls = imageUrls;
        this.caption = caption;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    // Database key of this post under profiles/{userId}/posts; set after reading, never stored
    private String postId;

    // likes/{uid}: true for everyone who liked it; comments/{pushId}: {userId, text, timestamp}
    private Map<String, Boolean> likes = new HashMap<>();
    private Map<String, Object> comments = new HashMap<>();

    @Exclude
    public String getPostId() {
        return postId;
    }

    @Exclude
    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes != null ? likes : new HashMap<>();
    }

    public Map<String, Object> getComments() {
        return comments;
    }

    public void setComments(Map<String, Object> comments) {
        this.comments = comments != null ? comments : new HashMap<>();
    }

}