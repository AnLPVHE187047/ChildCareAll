package com.example.childcare.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.childcare.R;
import com.example.childcare.models.Appointment;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;
import com.google.android.material.button.MaterialButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentDetailActivity extends AppCompatActivity {

    private TextView tvServiceName, tvStaffName, tvDate, tvTime, tvAddress, tvStatus, tvCreatedAt;
    private MaterialButton btnBack;
    private ProgressBar progressBar;
    private View contentLayout;
    private MaterialButton btnCancelAppointment;
    private Appointment currentAppointment;
    private String role;
    private int appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        initViews();
        setupToolbar();

        appointmentId = getIntent().getIntExtra("appointmentId", -1);
        role = getIntent().getStringExtra("role");
        if (appointmentId == -1) {
            Toast.makeText(this, "Invalid appointment ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadAppointmentDetail();
    }

    private void initViews() {
        tvServiceName = findViewById(R.id.tvServiceName);
        tvStaffName = findViewById(R.id.tvStaffName);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvAddress = findViewById(R.id.tvAddress);
        tvStatus = findViewById(R.id.tvStatus);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        btnBack = findViewById(R.id.btnBack);
        btnCancelAppointment = findViewById(R.id.btnCancelAppointment);
        progressBar = findViewById(R.id.progressBar);
        contentLayout = findViewById(R.id.contentLayout);
    }

    private void setupToolbar() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadAppointmentDetail() {
        showLoading(true);

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getAppointmentById(appointmentId).enqueue(new Callback<Appointment>() {
            @Override
            public void onResponse(Call<Appointment> call, Response<Appointment> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    displayAppointment(response.body());
                } else {
                    Toast.makeText(AppointmentDetailActivity.this,
                            "Failed to load appointment details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Appointment> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AppointmentDetailActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayAppointment(Appointment apt) {
        tvServiceName.setText(apt.getServiceName());
        tvStaffName.setText(apt.getStaffName() != null ? apt.getStaffName() : "Not assigned");

        String formattedDate = formatDate(apt.getAppointmentDate());
        tvDate.setText(formattedDate);

        String formattedTime = formatTime(apt.getAppointmentTime());
        tvTime.setText(formattedTime);

        tvAddress.setText(apt.getAddress());

        tvStatus.setText(apt.getStatus());
        setStatusColor(tvStatus, apt.getStatus());
        currentAppointment = apt;

        if ("Staff".equalsIgnoreCase(role)) {
            btnCancelAppointment.setVisibility(View.GONE);
        } else {
            if (apt.getStatus().equalsIgnoreCase("pending")) {
                btnCancelAppointment.setVisibility(View.VISIBLE);
                btnCancelAppointment.setOnClickListener(v -> confirmCancelAppointment());
            } else {
                btnCancelAppointment.setVisibility(View.GONE);
            }
        }

        String createdAt = formatDateTime(apt.getCreatedAt());
        tvCreatedAt.setText("Booked on: " + createdAt);
    }

    private void confirmCancelAppointment() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes", (dialog, which) -> cancelAppointment())
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelAppointment() {
        showLoading(true);
        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.cancelAppointment(currentAppointment.getAppointmentID()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(AppointmentDetailActivity.this, "Appointment cancelled successfully.", Toast.LENGTH_SHORT).show();
                    btnCancelAppointment.setVisibility(View.GONE);
                    tvStatus.setText("Cancelled");
                    setStatusColor(tvStatus, "cancelled");
                } else {
                    Toast.makeText(AppointmentDetailActivity.this, "Failed to cancel appointment.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AppointmentDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US);
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private String formatTime(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            if (parts.length >= 2) {
                int hour = Integer.parseInt(parts[0]);
                int min = Integer.parseInt(parts[1]);
                return String.format(Locale.US, "%02d:%02d", hour, min);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeStr;
    }

    private String formatDateTime(String dateTimeStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US);
            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateTimeStr;
        }
    }

    private void setStatusColor(TextView tvStatus, String status) {
        switch (status.toLowerCase()) {
            case "pending":
                tvStatus.setTextColor(Color.parseColor("#FF9800"));
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "confirmed":
                tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
                break;
            case "completed":
                tvStatus.setTextColor(Color.parseColor("#2196F3"));
                tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                break;
            case "cancelled":
                tvStatus.setTextColor(Color.parseColor("#F44336"));
                tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                break;
            default:
                tvStatus.setTextColor(Color.parseColor("#757575"));
                break;
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}