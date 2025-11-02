package com.example.childcare.models;

public class Service {
    private int serviceID;
    private String name;
    private String description;
    private double price;
    private int durationMinutes;
    private String imageUrl;

    public int getServiceID() { return serviceID; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getImageUrl() { return imageUrl; }
}
