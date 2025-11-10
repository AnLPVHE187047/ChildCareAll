package com.example.childcare.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.childcare.R;
import com.example.childcare.adapters.FeedbackAdapter;
import com.example.childcare.models.AverageRatingResponse;
import com.example.childcare.models.Feedback;
import com.example.childcare.models.FeedbackStaff;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffFeedbackActivity extends AppCompatActivity {

    private RecyclerView rvFeedbacks;
    private ProgressBar progressBar;
    private LinearLayout tvEmptyState;
    private TextView tvAverageRating, tvTotalFeedbacks;
    private RatingBar ratingBarAverage;

    private FeedbackAdapter adapter;
    private List<FeedbackStaff> feedbackList = new ArrayList<>();
    private int staffId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_feedback);

        // Initialize views
        rvFeedbacks = findViewById(R.id.rvFeedbacks);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalFeedbacks = findViewById(R.id.tvTotalFeedbacks);
        ratingBarAverage = findViewById(R.id.ratingBarAverage);

        // Setup RecyclerView
        rvFeedbacks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeedbackAdapter(this, feedbackList);
        rvFeedbacks.setAdapter(adapter);

        // Setup toolbar
        findViewById(R.id.toolbarFeedback).setOnClickListener(v -> onBackPressed());

        // Get staff ID
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userID", 0);

        fetchStaffId(userId);
    }

    private void fetchStaffId(int userId) {
        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getStaffIdByUser(userId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    staffId = response.body();
                    loadFeedbacks();
                    loadAverageRating();
                } else {
                    Toast.makeText(StaffFeedbackActivity.this, "Không tìm thấy thông tin nhân viên", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(StaffFeedbackActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFeedbacks() {
        if (staffId == 0) return;

        showLoading(true);

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getStaffFeedbacks(staffId).enqueue(new Callback<List<FeedbackStaff>>() {
            @Override
            public void onResponse(Call<List<FeedbackStaff>> call, Response<List<FeedbackStaff>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    feedbackList.clear();
                    feedbackList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    tvTotalFeedbacks.setText(feedbackList.size() + " đánh giá");
                    showEmptyState(feedbackList.isEmpty());
                } else {
                    showEmptyState(true);
                    Toast.makeText(StaffFeedbackActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FeedbackStaff>> call, Throwable t) {
                showLoading(false);
                showEmptyState(true);
                Toast.makeText(StaffFeedbackActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadAverageRating() {
        if (staffId == 0) return;

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getStaffAverageRating(staffId).enqueue(new Callback<AverageRatingResponse>() {
            @Override
            public void onResponse(Call<AverageRatingResponse> call, Response<AverageRatingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double rating = response.body().getAverageRating();
                    tvAverageRating.setText(String.format("%.1f", rating));
                    ratingBarAverage.setRating((float) rating);
                }
            }

            @Override
            public void onFailure(Call<AverageRatingResponse> call, Throwable t) {
                // Silent fail for rating
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvFeedbacks.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        rvFeedbacks.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}