package com.example.childcare.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.childcare.R;
import com.example.childcare.models.UserResponse;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;
import com.example.childcare.utils.FileUtils;

import java.io.File;

import okhttp3.*;
import retrofit2.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private EditText etFullName, etEmail, etPhone;
    private TextView tvRole;
    private Button btnSave;
    private Uri selectedImageUri = null;
    private int userId;

    private static final int PICK_IMAGE_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgProfile = findViewById(R.id.imgProfile);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        tvRole = findViewById(R.id.tvRole);
        btnSave = findViewById(R.id.btnSave);

        // 🔸 Lấy userID từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userID", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProfile();

        imgProfile.setOnClickListener(v -> chooseImage());
        btnSave.setOnClickListener(v -> updateProfile());
    }

    private void loadUserProfile() {
        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);

        api.getUserById(userId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    etFullName.setText(user.getFullName());
                    etEmail.setText(user.getEmail());
                    etPhone.setText(user.getPhone());
                    tvRole.setText("Role: " + user.getRole());

                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                        String imageUrl = user.getImageUrl().replace("localhost", "10.0.2.2");

                        Glide.with(ProfileActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.default_avatar)
                                .into(imgProfile);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgProfile.setImageURI(selectedImageUri);
        }
    }

    private void updateProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);


        RequestBody fullNameBody = RequestBody.create(MediaType.parse("text/plain"), fullName);
        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody phoneBody = RequestBody.create(MediaType.parse("text/plain"), phone);

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            String path = FileUtils.getPath(this, selectedImageUri);
            if (path != null) {
                File file = new File(path);
                RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
                imagePart = MultipartBody.Part.createFormData("imageFile", file.getName(), reqFile);
            }
        }

        api.updateUserProfile(userId, fullNameBody, emailBody, phoneBody, imagePart)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            loadUserProfile();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
