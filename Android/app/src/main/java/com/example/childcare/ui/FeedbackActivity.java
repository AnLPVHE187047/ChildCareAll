package com.example.childcare.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.childcare.R;
import com.example.childcare.models.AppointmentFeedbackDTO;
import com.example.childcare.models.Feedback;
import com.example.childcare.models.FeedbackCreateRequest;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";

    private TextView tvServiceName, tvStaffName, tvDate;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmit;
    private ProgressBar progressBar;

    private int appointmentId;
    private int staffId;
    private int userId;
    private String serviceName;
    private String staffName;
    private String appointmentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        initViews();
        getDataFromIntent();
        setupToolbar();
        displayAppointmentInfo();
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
    }

    private void getDataFromIntent() {
        appointmentId = getIntent().getIntExtra("appointmentId", 0);
        staffId = getIntent().getIntExtra("staffId", 0);
        serviceName = getIntent().getStringExtra("serviceName");
        staffName = getIntent().getStringExtra("staffName");
        appointmentDate = getIntent().getStringExtra("appointmentDate");

        Log.d(TAG, "Intent data received: appointmentId=" + appointmentId + ", staffId=" + staffId +
                ", serviceName=" + serviceName + ", staffName=" + staffName + ", appointmentDate=" + appointmentDate);

        // Get userId from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userID", 0);

        Log.d(TAG, "userId from SharedPreferences: " + userId);

        // Validate dữ liệu
        if (appointmentId == 0 || userId == 0) {
            Log.e(TAG, "Dữ liệu không hợp lệ: appointmentId=" + appointmentId + ", userId=" + userId);
            Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (staffId == 0) {
            Log.w(TAG, "Không tìm thấy thông tin nhân viên (staffId=0)");
            Toast.makeText(this, "Không tìm thấy thông tin nhân viên", Toast.LENGTH_SHORT).show();
            // Có thể finish() hoặc disable nút submit
        }
    }

    private void setupToolbar() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private void displayAppointmentInfo() {
        tvServiceName.setText(serviceName);
        tvStaffName.setText("Staff: " + staffName);
        tvDate.setText("Date: " + appointmentDate);

        checkIfAlreadyFeedback();
    }

    private void checkIfAlreadyFeedback() {
        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getCompletedAppointmentsForFeedback(userId).enqueue(new Callback<List<AppointmentFeedbackDTO>>() {
            @Override
            public void onResponse(Call<List<AppointmentFeedbackDTO>> call, Response<List<AppointmentFeedbackDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AppointmentFeedbackDTO> list = response.body();
                    boolean already = false;
                    for (AppointmentFeedbackDTO a : list) {
                        if (a.getAppointmentID() == appointmentId && a.isFeedbackGiven()) {
                            already = true;
                            break;
                        }
                    }

                    if (already) {
                        btnSubmit.setText("Đã đánh giá");
                        btnSubmit.setEnabled(false);
                    } else {
                        btnSubmit.setText("⭐ Đánh giá dịch vụ");
                        btnSubmit.setEnabled(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AppointmentFeedbackDTO>> call, Throwable t) {
                Log.e(TAG, "Lỗi check feedback", t);
                btnSubmit.setText("⭐ Đánh giá dịch vụ");
                btnSubmit.setEnabled(true);
            }
        });
    }


    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        float rating = ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        Log.d(TAG, "Submit clicked: rating=" + rating + ", comment=" + comment);

        // Validation
        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Rating = 0, không gửi feedback");
            return;
        }

        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nhận xét", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Comment rỗng, không gửi feedback");
            return;
        }

        if (staffId == 0) {
            Toast.makeText(this, "Không tìm thấy thông tin nhân viên", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "staffId = 0, không gửi feedback");
            return;
        }

        showLoading(true);
        btnSubmit.setEnabled(false);

        FeedbackCreateRequest request = new FeedbackCreateRequest(
                userId,
                staffId,
                (int) rating,
                comment
        );

        Log.d(TAG, "Sending feedback request: " + request.toString());

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        Call<Feedback> call = api.createFeedback(request);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(Call<Feedback> call, Response<Feedback> response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    btnSubmit.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "Feedback response successful: " + response.body());
                        Toast.makeText(FeedbackActivity.this,
                                "Đánh giá đã được gửi thành công!", Toast.LENGTH_SHORT).show();

                        new android.os.Handler().postDelayed(() -> {
                            setResult(RESULT_OK);
                            finish();
                        }, 500);
                    } else {
                        String errorMsg = "Không thể gửi đánh giá";
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Feedback response errorBody: " + errorBody);
                                if (errorBody.contains("already given feedback")) {
                                    errorMsg = "Bạn đã đánh giá nhân viên này rồi";
                                } else if (errorBody.contains("completed appointments")) {
                                    errorMsg = "Bạn chỉ có thể đánh giá sau khi hoàn thành dịch vụ";
                                } else {
                                    errorMsg = errorBody;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing errorBody", e);
                        }
                        Toast.makeText(FeedbackActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<Feedback> call, Throwable t) {
                runOnUiThread(() -> {
                    showLoading(false);
                    btnSubmit.setEnabled(true);

                    String errorMsg = "Lỗi kết nối: ";
                    if (t.getMessage() != null) {
                        errorMsg += t.getMessage();
                    } else {
                        errorMsg += "Không thể kết nối đến server";
                    }

                    Log.e(TAG, "Feedback request failed", t);
                    Toast.makeText(FeedbackActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
    }
}
