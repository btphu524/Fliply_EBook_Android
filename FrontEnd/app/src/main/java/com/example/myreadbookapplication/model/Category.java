package com.example.myreadbookapplication.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Category implements Serializable {
    @SerializedName("_id")
    private int id;
    private String name;
    @SerializedName("image_url")
    private String imageUrl;
    private String status; //is active or not
    private String updatedAt; // timestamp

    //Default constructor
    public Category() {}

    //Constructor
    public Category(int id, String name, String imageUrl, String status, String updatedAt) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    //getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

}
