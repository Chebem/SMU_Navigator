package com.example.smunavigator2.Domain;

import androidx.annotation.NonNull;

public class CityLocation {
    private int Id;
    private String loc;  // lowercase 'l' to match Firebase key


    // 🔧 No-argument constructor for Firebase
    public CityLocation() {}

    public CityLocation(int id, String loc) {
       this.Id = id;
       this.loc = loc;// Default constructor required for calls to DataSnapshot.getValue(Location.class)
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc)
    {
        this.loc = loc;
    }


    @NonNull
    @Override
    public String toString() {
        return loc;
    }
}
