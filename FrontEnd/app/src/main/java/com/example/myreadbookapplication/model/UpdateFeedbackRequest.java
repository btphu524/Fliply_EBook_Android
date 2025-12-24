package com.example.myreadbookapplication.model;

import com.google.gson.annotations.SerializedName;

public class UpdateFeedbackRequest {
    @SerializedName("comment")
    private String comment;

    public UpdateFeedbackRequest() {}

    public UpdateFeedbackRequest(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
