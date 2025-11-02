// models/AppointmentCreateRequest.java
package com.example.childcare.models;

public class AppointmentCreateRequest {
    private int serviceID;
    private int staffID;
    private String appointmentDate;
    private String appointmentTime;
    private String address;

    public AppointmentCreateRequest(int serviceID, int staffID, String appointmentDate, String appointmentTime, String address) {
        this.serviceID = serviceID;
        this.staffID = staffID;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime + ":00"; // chuẩn TimeSpan
        this.address = address;
    }
}
