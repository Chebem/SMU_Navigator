package com.example.smunavigator2.Domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileModel {
    public String profileName;
    public String profileImage;
    public String department;
    public String about;
    public int followersNum;
    public int followingNum;
    public int likes;
    private static String caption;
    private static long timestamp;
    private String userId;
    public ArrayList<Follower> followers;
    public Map<String, Post> posts;  // ✅ Correct type

    public ProfileModel() {
    }

    public ProfileModel(String profileName,
                        String profileImage,
                        String department,
                        String about,
                        int followersNum,
                        int followingNum,
                        int likes,
                        ArrayList<Follower> followers,
                        Map<String, Post> posts) {  // ✅ FIX: use Map here too
        this.profileName = profileName;
        this.profileImage = profileImage;
        this.department = department;
        this.about = about;
        this.followersNum = followersNum;
        this.followingNum = followingNum;
        this.likes = likes;
        this.followers = followers;
        this.posts = posts;


    }

    public static class Follower {
        public String imageUrl;
        public String name;

        public Follower() {
        }

        public Follower(String imageUrl, String name) {
            this.imageUrl = imageUrl;
            this.name = name;
        }
    }

    public static class Post {
        public List<String> imageUrls;
        private String caption;
        private long timestamp;
        private String userId;

        public Post() {}

        public Post(List<String> imageUrls) {
            this.imageUrls = imageUrls;
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

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}