package com.example.childcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.childcare.R;
import com.example.childcare.adapters.AppointmentAdapter;
import com.example.childcare.models.Appointment;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentHistoryActivity extends AppCompatActivity {

    private RecyclerView rvAppointments;
    private ProgressBar progressBar;
    private LinearLayout tvEmptyState;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_history);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadAppointments();
    }

    private void initViews() {
        rvAppointments = findViewById(R.id.rvAppointments);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupToolbar() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(this, appointmentList, appointment -> {
            // Click vào item -> xem chi tiết
            Intent intent = new Intent(AppointmentHistoryActivity.this, AppointmentDetailActivity.class);
            intent.putExtra("appointmentId", appointment.getAppointmentID());
            startActivity(intent);
        });
        rvAppointments.setAdapter(adapter);
    }

    private void loadAppointments() {
        showLoading(true);

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getMyAppointments().enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    appointmentList.clear();
                    appointmentList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (appointmentList.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                    }
                } else {
                    Toast.makeText(AppointmentHistoryActivity.this,
                            "Failed to load appointments", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AppointmentHistoryActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvAppointments.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        rvAppointments.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh khi quay lại activity
        loadAppointments();
    }
}