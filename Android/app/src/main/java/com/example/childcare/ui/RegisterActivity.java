package com.example.childcare.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import com.example.childcare.R;
import com.example.childcare.models.RegisterRequest;
import com.example.childcare.models.UserResponse;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.*;

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPhone, etPassword;
    Spinner spRole;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        spRole = findViewById(R.id.spRole);
        btnRegister = findViewById(R.id.btnRegister);

        // Spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Parent"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spRole.getSelectedItem().toString();

        if(fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please fill all required fields", Snackbar.LENGTH_LONG).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);
        RegisterRequest request = new RegisterRequest(fullName, email, phone, password, role);

        api.register(request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    // đọc body lỗi
                    String errorMsg = "Registration failed";
                    try {
                        if(response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Snackbar.make(findViewById(android.R.id.content), errorMsg, Snackbar.LENGTH_LONG).show();
                }
            }


            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
