package com.example.childcare.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childcare.R;
import com.example.childcare.adapters.StaffAppointmentAdapter;
import com.example.childcare.models.Appointment;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private RecyclerView rvAppointments;
    private ProgressBar progressBar;
    private LinearLayout tvEmptyState;
    private StaffAppointmentAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();
    private Timer pollingTimer;
    private Integer selectedDayOfWeek = null;
    private EditText edtSearchCustomer;
    private Spinner spnMonth, spnWeek;
    private int staffId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout);
        rvAppointments = findViewById(R.id.rvAppointments);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        edtSearchCustomer = findViewById(R.id.edtSearchCustomer);
        spnMonth = findViewById(R.id.spnMonth);
        spnWeek = findViewById(R.id.spnWeek);

        // Setup RecyclerView
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StaffAppointmentAdapter(this, appointmentList);
        rvAppointments.setAdapter(adapter);

        adapter.setOnItemClickListener(new StaffAppointmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Appointment appointment) {
                Intent intent = new Intent(StaffHomeActivity.this, AppointmentDetailActivity.class);
                intent.putExtra("appointmentId", appointment.getAppointmentID());
                intent.putExtra("role", "Staff");
                startActivity(intent);
            }

            @Override
            public void onChangeStatus(Appointment appointment, String newStatus) {
                changeAppointmentStatus(appointment.getAppointmentID(), newStatus);
            }
        });

        // Setup Navigation Drawer
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        // Setup toolbar with drawer toggle
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarStaff);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup other components
        setupSpinners();
        setupDaySelector();
        setupListeners();

        // Load data
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userID", 0);

        startPolling();
        fetchStaffId(userId);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_schedule) {
            // Already on this screen
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_feedback) {
            startActivity(new Intent(this, StaffFeedbackActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_logout) {
            logout();
            return true;
        }

        return false;
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // ... (giữ nguyên các phương thức còn lại từ code cũ)

    private void changeAppointmentStatus(int appointmentId, String newStatus) {
        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        showLoading(true);

        api.updateAppointmentStatus(appointmentId, newStatus)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        showLoading(false);
                        if (response.isSuccessful()) {
                            String message = getSuccessMessage(newStatus);
                            Snackbar.make(rvAppointments, message, Snackbar.LENGTH_LONG).show();
                            loadAppointments();
                        } else {
                            String errorMsg = "Không thể cập nhật trạng thái";
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    if (errorBody.contains("message")) {
                                        int start = errorBody.indexOf("\"message\":\"") + 11;
                                        int end = errorBody.indexOf("\"", start);
                                        if (start > 10 && end > start) {
                                            errorMsg = errorBody.substring(start, end);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                errorMsg += " (Code: " + response.code() + ")";
                            }
                            Snackbar.make(rvAppointments, errorMsg, Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        showLoading(false);
                        Snackbar.make(rvAppointments, "Lỗi kết nối: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }


    private String getSuccessMessage(String status) {
        switch (status) {
            case "Confirmed": return "✓ Đã xác nhận lịch hẹn";
            case "Completed": return "✓ Đã hoàn tất lịch hẹn";
            case "Cancelled": return "✓ Đã hủy lịch hẹn";
            default: return "✓ Cập nhật thành công";
        }
    }

    private void setupDaySelector() {
        LinearLayout dayContainer = findViewById(R.id.lnrDaySelector);
        dayContainer.removeAllViews();

        Calendar cal = Calendar.getInstance();
        int todayJava = cal.get(Calendar.DAY_OF_WEEK);
        String[] daysLabel = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};

        for (int i = 0; i < 7; i++) {
            final int idx = i;
            Button btnDay = new Button(this);
            btnDay.setText(daysLabel[i]);
            btnDay.setAllCaps(false);
            btnDay.setTextSize(14f);
            btnDay.setPadding(24, 12, 24, 12);
            btnDay.setBackgroundResource(R.drawable.bg_day_selector);

            if (i == 0) {
                if (todayJava == Calendar.SUNDAY) {
                    btnDay.setBackgroundTintList(getColorStateList(R.color.teal_700));
                    btnDay.setTextColor(Color.WHITE);
                } else {
                    btnDay.setTextColor(Color.BLACK);
                }
            } else {
                if (todayJava == i + 1) {
                    btnDay.setBackgroundTintList(getColorStateList(R.color.teal_700));
                    btnDay.setTextColor(Color.WHITE);
                } else {
                    btnDay.setTextColor(Color.BLACK);
                }
            }

            btnDay.setOnClickListener(v -> {
                for (int j = 0; j < dayContainer.getChildCount(); j++) {
                    Button b = (Button) dayContainer.getChildAt(j);
                    b.setBackgroundTintList(getColorStateList(android.R.color.transparent));
                    b.setTextColor(Color.BLACK);
                }
                btnDay.setBackgroundTintList(getColorStateList(R.color.teal_700));
                btnDay.setTextColor(Color.WHITE);

                selectedDayOfWeek = (idx == 0) ? 7 : idx;
                loadAppointments();
            });

            dayContainer.addView(btnDay);
        }

        Button btnAll = new Button(this);
        btnAll.setText("Tất cả");
        btnAll.setAllCaps(false);
        btnAll.setTextSize(14f);
        btnAll.setPadding(24, 12, 24, 12);
        btnAll.setBackgroundResource(R.drawable.bg_day_selector);
        btnAll.setOnClickListener(v -> {
            for (int j = 0; j < dayContainer.getChildCount(); j++) {
                Button b = (Button) dayContainer.getChildAt(j);
                b.setBackgroundTintList(getColorStateList(android.R.color.transparent));
                b.setTextColor(Color.BLACK);
            }
            selectedDayOfWeek = null;
            loadAppointments();
        });
        dayContainer.addView(btnAll, 0);
    }

    private void startPolling() {
        pollingTimer = new Timer();
        pollingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> loadAppointments());
            }
        }, 0, 60000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pollingTimer != null) {
            pollingTimer.cancel();
            pollingTimer = null;
        }
    }

    private void setupListeners() {
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

        spnMonth.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateWeekSpinner();
                loadAppointments();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

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
                new String[]{"Tất cả", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                        "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"});
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMonth.setAdapter(monthAdapter);

        Calendar cal = Calendar.getInstance();
        spnMonth.setSelection(cal.get(Calendar.MONTH) + 1);

        updateWeekSpinner();
    }

    private void updateWeekSpinner() {
        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Tất cả", "Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4", "Tuần 5"});
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
        if (customerName.isEmpty()) customerName = null;

        Integer month = getSelectedMonth();
        Integer week = getSelectedWeek();

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getStaffAppointments(staffId, customerName, month, week, selectedDayOfWeek)
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
                            Toast.makeText(StaffHomeActivity.this, "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Appointment>> call, Throwable t) {
                        showLoading(false);
                        showEmptyState(true);
                        Toast.makeText(StaffHomeActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    private Integer getSelectedMonth() {
        int position = spnMonth.getSelectedItemPosition();
        return position == 0 ? null : position;
    }

    private Integer getSelectedWeek() {
        int position = spnWeek.getSelectedItemPosition();
        return position == 0 ? null : position;
    }
}