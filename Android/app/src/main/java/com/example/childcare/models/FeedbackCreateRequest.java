package com.example.childcare.models;

import com.google.gson.annotations.SerializedName;

public class FeedbackCreateRequest {
    @SerializedName("UserID")
    private int userID;

    @SerializedName("StaffID")
    private Integer staffID;

    @SerializedName("Rating")
    private int rating;

    @SerializedName("Comment")
    private String comment;

    public FeedbackCreateRequest(int userID, Integer staffID, int rating, String comment) {
        this.userID = userID;
        this.staffID = staffID;
        this.rating = rating;
        this.comment = comment;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public Integer getStaffID() {
        return staffID;
    }

    public void setStaffID(Integer staffID) {
        this.staffID = staffID;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}