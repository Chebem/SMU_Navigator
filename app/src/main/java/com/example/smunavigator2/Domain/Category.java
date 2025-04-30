package com.example.smunavigator2.Domain;

public class Category
{
    private int Id;

    // ✅ Default constructor required for Firebase
    public Category() {
    }

    // Optional: Constructor for your own use
    public Category(int id, String imagePath, String name) {
        this.Id = id;
        this.ImagePath = imagePath;
        this.Name = name;
    }


    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Category(int id) {
        Id = id;
    }

    private String ImagePath;
    private String  Name;
}
