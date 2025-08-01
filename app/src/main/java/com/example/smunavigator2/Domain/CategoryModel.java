package com.example.smunavigator2.Domain;

import java.io.Serializable;

public class CategoryModel implements Serializable {
    private String id;
    private String nameKo;
    private String nameEn;
    private String ImagePath;
    private String categoryId;  // <--- ADD THIS

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    // Required empty constructor for Firebase
    public CategoryModel() {}

    public CategoryModel(String id, String nameKo, String nameEn, String imagePath) {
        this.id = id;
        this.nameKo = nameKo;
        this.nameEn = nameEn;
        this.ImagePath = ImagePath;
        this.categoryId = categoryId;
    }

    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getNameKo() {
        return nameKo;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNameKo(String nameKo) {
        this.nameKo = nameKo;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public void setImagePath(String ImagePath) {
        this.ImagePath = ImagePath;
    }
}