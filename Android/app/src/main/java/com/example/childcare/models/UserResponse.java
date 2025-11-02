package com.example.childcare.models;

public class UserResponse {
    private int userID;
    private String fullName;
    private String email;
    private String role;
    private String token;
    public int getUserID() { return userID; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getToken() { return token; }        // <-- getter
    public void setToken(String token) { this.token = token; }
}
