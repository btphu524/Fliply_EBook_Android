package com.example.myreadbookapplication.model;

import com.google.gson.annotations.SerializedName;

public class LogoutRequest {
    @SerializedName("email")
    private String email;

    public LogoutRequest() {}

    public LogoutRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
