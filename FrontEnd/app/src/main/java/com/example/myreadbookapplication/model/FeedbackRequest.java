package com.example.myreadbookapplication.model;

import com.google.gson.annotations.SerializedName;

public class FeedbackRequest {
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("comment")
    private String comment;

    public FeedbackRequest() {}

    public FeedbackRequest(String fullName, String phoneNumber, String email, String comment) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.comment = comment;
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
}
