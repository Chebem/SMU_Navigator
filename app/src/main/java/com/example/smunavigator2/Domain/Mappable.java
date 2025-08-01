package com.example.smunavigator2.Domain;

public interface Mappable {
    double getLat();
    double getLng();
    String getName();
    String getType();
    String getImagePath();

    String getDescription(); // Optional, if you want to include a description

    String getCategory(); // Optional, if you want to include a category
}
