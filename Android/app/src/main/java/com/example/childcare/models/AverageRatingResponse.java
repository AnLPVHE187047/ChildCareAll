package com.example.childcare.models;

import com.google.gson.annotations.SerializedName;

public class AverageRatingResponse {
    @SerializedName("averageRating")
    private double averageRating;

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
}