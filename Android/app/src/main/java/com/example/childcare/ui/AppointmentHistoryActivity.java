package com.example.childcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

    // Filter UI
    private EditText etSearchName;
    private Spinner spStatus;
    private Spinner spMonth;

    private String selectedStatus = null;
    private Integer selectedMonth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_history);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        loadAppointments();
    }

    private void initViews() {
        rvAppointments = findViewById(R.id.rvAppointments);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        etSearchName = findViewById(R.id.etSearchName);
        spStatus = findViewById(R.id.spStatus);
        spMonth = findViewById(R.id.spMonth);
    }

    private void setupToolbar() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(this, appointmentList, appointment -> {
            Intent intent = new Intent(AppointmentHistoryActivity.this, AppointmentDetailActivity.class);
            intent.putExtra("appointmentId", appointment.getAppointmentID());
            startActivity(intent);
        });
        rvAppointments.setAdapter(adapter);
    }

    private void setupFilters() {
        // Status Spinner
        String[] statuses = {"All", "Pending", "Confirmed", "Completed", "Cancelled"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(statusAdapter);

        spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = position == 0 ? null : statuses[position];
                loadAppointments();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Month Spinner
        String[] months = {"All", "1","2","3","4","5","6","7","8","9","10","11","12"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);

        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = position == 0 ? null : position;
                loadAppointments();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Search by name
        etSearchName.setOnEditorActionListener((v, actionId, event) -> {
            loadAppointments();
            return true;
        });
    }

    private void loadAppointments() {
        showLoading(true);

        String nameFilter = etSearchName.getText().toString().trim();

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getMyAppointments(nameFilter, selectedMonth, null, selectedStatus)
                .enqueue(new Callback<List<Appointment>>() {
                    @Override
                    public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            appointmentList.clear();
                            appointmentList.addAll(response.body());
                            adapter.notifyDataSetChanged();

                            showEmptyState(appointmentList.isEmpty());
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
        loadAppointments();
    }
}
