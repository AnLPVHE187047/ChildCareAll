// File: app/src/main/java/com/example/childcare/models/AppointmentFeedbackDTO.java
package com.example.childcare.models;

public class AppointmentFeedbackDTO {
    private int appointmentID;
    private String serviceName;
    private String staffName;
    private String appointmentDate;
    private String appointmentTime;
    private boolean isFeedbackGiven;

    // Getters and Setters
    public int getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(int appointmentID) {
        this.appointmentID = appointmentID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public boolean isFeedbackGiven() {
        return isFeedbackGiven;
    }

    public void setFeedbackGiven(boolean feedbackGiven) {
        isFeedbackGiven = feedbackGiven;
    }
}