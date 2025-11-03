package com.example.childcare.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childcare.R;
import com.example.childcare.adapters.StaffAppointmentAdapter;
import com.example.childcare.models.Appointment;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffHomeActivity extends AppCompatActivity {

    private RecyclerView rvAppointments;
    private ProgressBar progressBar;
    private LinearLayout tvEmptyState;
    private StaffAppointmentAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();

    private EditText edtSearchCustomer;
    private Spinner spnMonth, spnWeek;
    private int staffId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        rvAppointments = findViewById(R.id.rvAppointments);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        edtSearchCustomer = findViewById(R.id.edtSearchCustomer);
        spnMonth = findViewById(R.id.spnMonth);
        spnWeek = findViewById(R.id.spnWeek);

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StaffAppointmentAdapter(this, appointmentList);
        rvAppointments.setAdapter(adapter);
        adapter.setOnItemClickListener(appointment -> {
            Intent intent = new Intent(StaffHomeActivity.this, AppointmentDetailActivity.class);
            intent.putExtra("appointmentId", appointment.getAppointmentID());
            intent.putExtra("role", "Staff");
            startActivity(intent);
        });
        // Spinner setup
        setupSpinners();

        // Load staff ID
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userID", 0);
        fetchStaffId(userId);
        setupListeners();
        findViewById(R.id.toolbarStaff).setOnClickListener(v -> onBackPressed());
    }
    private void setupListeners() {
        // Search customer theo tên
        edtSearchCustomer.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                loadAppointments();
            }
        });

        // Spinner tháng
        spnMonth.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadAppointments();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // Spinner tuần
        spnWeek.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadAppointments();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void setupSpinners() {
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"});
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMonth.setAdapter(monthAdapter);

        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "1", "2", "3", "4", "5"});
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnWeek.setAdapter(weekAdapter);
    }

    private void fetchStaffId(int userId) {
        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getStaffIdByUser(userId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    staffId = response.body();
                    loadAppointments();
                } else {
                    Toast.makeText(StaffHomeActivity.this, "Không tìm thấy StaffID", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(StaffHomeActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAppointments() {
        if (staffId == 0) return;

        showLoading(true);

        String customerName = edtSearchCustomer.getText().toString().trim();
        Integer month = getSelectedInt(spnMonth);
        Integer week = getSelectedInt(spnWeek);

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getStaffAppointments(staffId, customerName, month, week)
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
                            showEmptyState(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Appointment>> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(StaffHomeActivity.this, "Lỗi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    private Integer getSelectedInt(Spinner spinner) {
        String value = spinner.getSelectedItem() != null ? spinner.getSelectedItem().toString() : "";
        try {
            return value.equals("All") ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
