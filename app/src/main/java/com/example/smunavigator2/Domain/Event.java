package com.example.smunavigator2.Domain;

public class Event {
    private String title;
    private String time; // Optional – you can change it to null or "" if unused

    public Event() {
        // Required for Firebase
    }

    public Event(String title, String time) {
        this.title = title;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
