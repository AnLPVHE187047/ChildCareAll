package com.example.childcare.models;

public class RegisterRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String role;

    public RegisterRequest(String fullName, String email, String phone, String password, String role) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }
}