package com.example.smunavigator2.Utils;

public class TimeUtils {
    public static String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        long minutes = diff / 60000;
        if (minutes < 1) return "Just now";
        else if (minutes < 60) return minutes + " mins ago";
        else if (minutes < 1440) return (minutes / 60) + " hrs ago";
        else return (minutes / 1440) + " days ago";
    }
}