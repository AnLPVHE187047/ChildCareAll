package com.example.childcare.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.example.childcare.R;
import com.example.childcare.models.Feedback;
import com.example.childcare.models.FeedbackCreateRequest;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;
import retrofit2.*;

public class FeedbackActivity extends AppCompatActivity {

    private TextView tvServiceName, tvStaffName, tvDate;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmit;
    private ProgressBar progressBar;

    private int appointmentId;
    private int staffId;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        initViews();
        loadIntentData();
        setupToolbar();
        setupSubmitButton();
    }

    private void initViews() {
        tvServiceName = findViewById(R.id.tvServiceName);
        tvStaffName = findViewById(R.id.tvStaffName);
        tvDate = findViewById(R.id.tvDate);
        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        // Get userId from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userID", 0);
    }

    private void loadIntentData() {
        appointmentId = getIntent().getIntExtra("appointmentId", 0);
        staffId = getIntent().getIntExtra("staffId", 0);
        String serviceName = getIntent().getStringExtra("serviceName");
        String staffName = getIntent().getStringExtra("staffName");
        String date = getIntent().getStringExtra("appointmentDate");

        tvServiceName.setText(serviceName);
        tvStaffName.setText("Nhân viên: " + staffName);
        tvDate.setText("Ngày: " + date);
    }

    private void setupToolbar() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        float rating = ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Tạo request với AppointmentID
        FeedbackCreateRequest request = new FeedbackCreateRequest(
                userId,
                staffId,
                appointmentId,  // ✅ Truyền appointmentId
                (int) rating,
                comment.isEmpty() ? null : comment
        );

        showLoading(true);

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.createFeedback(request).enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(Call<Feedback> call, Response<Feedback> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(FeedbackActivity.this,
                            "✓ Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null
                                ? response.errorBody().string()
                                : "Unknown error";

                        // Parse error message
                        if (errorBody.contains("already given feedback")) {
                            Toast.makeText(FeedbackActivity.this,
                                    "Bạn đã đánh giá lịch hẹn này rồi", Toast.LENGTH_LONG).show();
                        } else if (errorBody.contains("not completed")) {
                            Toast.makeText(FeedbackActivity.this,
                                    "Chỉ có thể đánh giá sau khi hoàn thành dịch vụ", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(FeedbackActivity.this,
                                    "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(FeedbackActivity.this,
                                "Gửi đánh giá thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Feedback> call, Throwable t) {
                showLoading(false);
                Toast.makeText(FeedbackActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
    }
}