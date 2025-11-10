package com.example.childcare.models;

import com.google.gson.annotations.SerializedName;

public class FeedbackStaff {
    @SerializedName("feedbackID")
    private int feedbackID;

    @SerializedName("userName")
    private String userName;

    @SerializedName("serviceName")
    private String serviceName;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("appointmentDate")
    private String appointmentDate;

    // Getters and Setters
    public int getFeedbackID() { return feedbackID; }
    public void setFeedbackID(int feedbackID) { this.feedbackID = feedbackID; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }
}