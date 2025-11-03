package com.example.childcare.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.childcare.R;
import com.example.childcare.models.UserResponse;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.*;
import retrofit2.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ImageView imgProfile;
    private EditText etFullName, etEmail, etPhone;
    private TextView tvRole;
    private Button btnSave;
    private Uri selectedImageUri = null;
    private int userId;
    private boolean isActive;

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
                    isActive = user.getIsActive();

                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                        String imageUrl = user.getImageUrl().replace("localhost", "10.0.2.2");
                        Log.d(TAG, "Loading image from: " + imageUrl);

                        Glide.with(ProfileActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.default_avatar)
                                .into(imgProfile);
                    }
                } else {
                    Log.e(TAG, "Failed to load profile. Code: " + response.code());
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "Error loading profile", t);
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a photo"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Log.d(TAG, "Selected image URI: " + selectedImageUri);
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

        Log.d(TAG, "Starting profile update...");
        Log.d(TAG, "UserID: " + userId);
        Log.d(TAG, "Selected Image URI: " + selectedImageUri);

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);

        RequestBody fullNameBody = RequestBody.create(MediaType.parse("text/plain"), fullName);
        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody phoneBody = RequestBody.create(MediaType.parse("text/plain"), phone);
        RequestBody isActiveBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(isActive));

        MultipartBody.Part imagePart = null;

        if (selectedImageUri != null) {
            try {
                Log.d(TAG, "Processing image...");

                // ✅ Tạo file tạm thời từ URI
                File tempFile = createTempFileFromUri(selectedImageUri);

                if (tempFile != null && tempFile.exists()) {
                    Log.d(TAG, "Temp file created: " + tempFile.getAbsolutePath());
                    Log.d(TAG, "File size: " + tempFile.length() + " bytes");

                    RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
                    imagePart = MultipartBody.Part.createFormData("imageFile", tempFile.getName(), reqFile);

                    Log.d(TAG, "Image part created successfully");
                } else {
                    Log.e(TAG, "Failed to create temp file");
                    Toast.makeText(this, "Cannot access image file", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing image", e);
                Toast.makeText(this, "Error reading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Log.d(TAG, "No image selected");
        }

        Log.d(TAG, "Sending API request...");

        api.updateUserProfile(userId, fullNameBody, emailBody, phoneBody, isActiveBody, imagePart)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.d(TAG, "API Response code: " + response.code());

                        if (response.isSuccessful()) {
                            Log.d(TAG, "Profile updated successfully");
                            Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            prefs.edit().putString("fullName", fullName).apply();

                            selectedImageUri = null;
                            loadUserProfile();
                        } else {
                            Log.e(TAG, "Update failed with code: " + response.code());
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                Log.e(TAG, "Error body: " + errorBody);
                            } catch (Exception e) {
                                Log.e(TAG, "Cannot read error body", e);
                            }
                            Toast.makeText(ProfileActivity.this, "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "API call failed", t);
                        Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * ✅ Tạo file tạm thời từ URI (giải quyết vấn đề Scoped Storage)
     */
    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Cannot open input stream from URI");
                return null;
            }

            // Tạo file tạm trong cache directory
            File tempFile = new File(getCacheDir(), "temp_upload_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            Log.d(TAG, "Temp file created successfully: " + tempFile.getAbsolutePath());
            return tempFile;

        } catch (Exception e) {
            Log.e(TAG, "Error creating temp file from URI", e);
            return null;
        }
    }
}