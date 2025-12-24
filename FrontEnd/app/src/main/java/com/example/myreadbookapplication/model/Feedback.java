package com.example.myreadbookapplication.model;

import com.google.gson.annotations.SerializedName;

public class Feedback {
    @SerializedName("_id")
    private String id; // Backend returns string _id from Firebase keys
    
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("comment")
    private String comment;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("createdAt")
    private long createdAt;
    
    @SerializedName("updatedAt")
    private long updatedAt;

    public Feedback() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", comment='" + comment + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
