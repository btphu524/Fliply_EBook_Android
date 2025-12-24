package com.example.myreadbookapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UpdateUserRequest {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatar;
    private List<String> preferences;

    // Constructors
    public UpdateUserRequest() {}

    public UpdateUserRequest(String fullName, String email, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }

    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", avatar='" + avatar + '\'' +
                ", preferences=" + preferences +
                '}';
    }
}
