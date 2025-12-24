package com.example.myreadbookapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CategoriesResponse {
    @SerializedName("categories")
    private List<Category> categories;

    // Getters/Setters
    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }
}