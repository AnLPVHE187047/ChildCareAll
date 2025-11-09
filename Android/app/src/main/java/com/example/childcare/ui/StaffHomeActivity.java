package com.example.childcare.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;

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
    private Timer pollingTimer;
    private Integer selectedDayOfWeek = null;
    private EditText edtSearchCustomer;
    private Spinner spnMonth, spnWeek;
    private int staffId = 0;

    // Lưu trữ thông tin tuần
    private static class WeekInfo {
        int weekNumber;
        String displayText;

        WeekInfo(int weekNumber, String displayText) {
            this.weekNumber = weekNumber;
            this.displayText = displayText;
        }

        @Override
        public String toString() {
            return displayText;
        }
    }

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

        // Spinner setup
        setupSpinners();
        setupDaySelector();

        // Load staff ID
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userID", 0);
        startPolling();
        fetchStaffId(userId);
        setupListeners();
        findViewById(R.id.toolbarStaff).setOnClickListener(v -> onBackPressed());
    }

    private void changeAppointmentStatus(int appointmentId, String newStatus) {
        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);

        // Show loading indicator (optional)
        showLoading(true);

        api.updateAppointmentStatus(appointmentId, newStatus)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        showLoading(false);

                        if (response.isSuccessful()) {
                            String message = getSuccessMessage(newStatus);
                            Toast.makeText(StaffHomeActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadAppointments(); // refresh list
                        } else {
                            // Parse error message from backend
                            String errorMsg = "Không thể cập nhật trạng thái";
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    // Try to extract message from JSON
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
                            Toast.makeText(StaffHomeActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(StaffHomeActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getSuccessMessage(String status) {
        switch (status) {
            case "Confirmed":
                return "✓ Đã xác nhận lịch hẹn";
            case "Completed":
                return "✓ Đã hoàn tất lịch hẹn";
            case "Cancelled":
                return "✓ Đã hủy lịch hẹn";
            default:
                return "✓ Cập nhật thành công";
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

            // Highlight today
            if (i == 0) { // CN (Sunday)
                if (todayJava == Calendar.SUNDAY) {
                    btnDay.setBackgroundTintList(getColorStateList(R.color.teal_700));
                    btnDay.setTextColor(Color.WHITE);
                } else {
                    btnDay.setTextColor(Color.BLACK);
                }
            } else { // T2-T7 (Monday-Saturday)
                if (todayJava == i + 1) {
                    btnDay.setBackgroundTintList(getColorStateList(R.color.teal_700));
                    btnDay.setTextColor(Color.WHITE);
                } else {
                    btnDay.setTextColor(Color.BLACK);
                }
            }

            btnDay.setOnClickListener(v -> {
                // Reset all buttons
                for (int j = 0; j < dayContainer.getChildCount(); j++) {
                    Button b = (Button) dayContainer.getChildAt(j);
                    b.setBackgroundTintList(getColorStateList(android.R.color.transparent));
                    b.setTextColor(Color.BLACK);
                }
                btnDay.setBackgroundTintList(getColorStateList(R.color.teal_700));
                btnDay.setTextColor(Color.WHITE);

                // Map to server format: 1=Mon, 2=Tue, ..., 7=Sun
                if (idx == 0) {
                    selectedDayOfWeek = 7; // Sunday
                } else {
                    selectedDayOfWeek = idx; // 1-6 = Mon-Sat
                }

                loadAppointments();
            });

            dayContainer.addView(btnDay);
        }

        // Add "All" button
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
        }, 0, 60000); // Poll every 60 seconds
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
                // Update week spinner when month changes
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
        // Month spinner
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Tất cả", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                        "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"});
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMonth.setAdapter(monthAdapter);

        // Set current month as default
        Calendar cal = Calendar.getInstance();
        spnMonth.setSelection(cal.get(Calendar.MONTH) + 1); // +1 vì có "Tất cả" ở đầu

        // Week spinner - will be populated based on selected month
        updateWeekSpinner();
    }

    /**
     * Update week spinner based on selected month
     * Display format: "1-7/11", "8-14/11", etc.
     */
    private void updateWeekSpinner() {
        Integer month = getSelectedMonth();

        if (month == null) {
            // Show generic weeks if no month selected
            ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item,
                    new String[]{"Tất cả", "Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4", "Tuần 5"});
            weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnWeek.setAdapter(weekAdapter);
            return;
        }

        // Calculate weeks for the selected month
        int year = Calendar.getInstance().get(Calendar.YEAR);
        List<WeekInfo> weeks = calculateWeeksInMonth(year, month);

        // Create display list with "Tất cả" option
        List<String> weekDisplayList = new ArrayList<>();
        weekDisplayList.add("Tất cả");
        for (WeekInfo week : weeks) {
            weekDisplayList.add(week.displayText);
        }

        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, weekDisplayList);
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnWeek.setAdapter(weekAdapter);
    }

    /**
     * Calculate weeks in a month with date ranges
     * Logic: Tuần bắt đầu từ thứ 2 (Monday), tuần 1 là tuần chứa ngày 1 của tháng
     *
     * VD: Tháng 11/2025 bắt đầu vào Thứ 7 (Saturday):
     * - Tuần 1: Thứ 7 (1/11) → Chủ nhật (2/11)
     * - Tuần 2: Thứ 2 (3/11) → Chủ nhật (9/11)
     * - Tuần 3: Thứ 2 (10/11) → Chủ nhật (16/11)
     * ...
     */
    private List<WeekInfo> calculateWeeksInMonth(int year, int month) {
        List<WeekInfo> weeks = new ArrayList<>();

        Calendar firstDayOfMonth = Calendar.getInstance();
        firstDayOfMonth.set(year, month - 1, 1); // month-1 vì Calendar.MONTH bắt đầu từ 0

        Calendar lastDayOfMonth = Calendar.getInstance();
        lastDayOfMonth.set(year, month - 1, firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

        // Tìm thứ 2 của tuần chứa ngày 1 (có thể là trước ngày 1)
        Calendar firstMonday = (Calendar) firstDayOfMonth.clone();
        int dayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK);

        // Tính số ngày cần lùi để về thứ 2
        // Calendar: Sunday=1, Monday=2, ..., Saturday=7
        int daysFromMonday = (dayOfWeek - Calendar.MONDAY + 7) % 7;
        firstMonday.add(Calendar.DAY_OF_MONTH, -daysFromMonday);

        // Duyệt từng tuần
        Calendar weekStart = (Calendar) firstMonday.clone();
        int weekNumber = 1;

        while (weekStart.get(Calendar.DAY_OF_MONTH) <= lastDayOfMonth.get(Calendar.DAY_OF_MONTH)
                || weekStart.get(Calendar.MONTH) < lastDayOfMonth.get(Calendar.MONTH)) {

            Calendar weekEnd = (Calendar) weekStart.clone();
            weekEnd.add(Calendar.DAY_OF_MONTH, 6); // Tuần có 7 ngày (từ T2 đến CN)

            // Xác định ngày bắt đầu và kết thúc trong tháng
            int startDay, endDay;

            // Nếu tuần bắt đầu trước tháng, lấy từ ngày 1
            if (weekStart.get(Calendar.MONTH) < firstDayOfMonth.get(Calendar.MONTH)) {
                startDay = 1;
            } else {
                startDay = weekStart.get(Calendar.DAY_OF_MONTH);
            }

            // Nếu tuần kết thúc sau tháng, lấy đến ngày cuối tháng
            if (weekEnd.get(Calendar.MONTH) > lastDayOfMonth.get(Calendar.MONTH)) {
                endDay = lastDayOfMonth.get(Calendar.DAY_OF_MONTH);
            } else {
                endDay = weekEnd.get(Calendar.DAY_OF_MONTH);
            }

            // Kiểm tra nếu tuần này có ít nhất 1 ngày trong tháng
            if (startDay >= 1 && startDay <= lastDayOfMonth.get(Calendar.DAY_OF_MONTH)) {
                String displayText = String.format("%d-%d/%d", startDay, endDay, month);
                weeks.add(new WeekInfo(weekNumber, displayText));
                weekNumber++;
            }

            // Chuyển sang tuần tiếp theo (cộng 7 ngày)
            weekStart.add(Calendar.DAY_OF_MONTH, 7);

            // Safety check: tối đa 6 tuần trong 1 tháng
            if (weekNumber > 6) break;

            // Nếu tuần tiếp theo đã ra khỏi tháng hoàn toàn thì dừng
            if (weekStart.get(Calendar.MONTH) > lastDayOfMonth.get(Calendar.MONTH)) {
                break;
            }
        }

        return weeks;
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

    /**
     * Get selected month as integer (1-12) or null if "Tất cả"
     */
    private Integer getSelectedMonth() {
        int position = spnMonth.getSelectedItemPosition();
        return position == 0 ? null : position; // 0 = "Tất cả", 1-12 = tháng 1-12
    }

    /**
     * Get selected week number (1-6) or null if "Tất cả"
     */
    private Integer getSelectedWeek() {
        int position = spnWeek.getSelectedItemPosition();
        return position == 0 ? null : position; // 0 = "Tất cả", 1-6 = tuần 1-6
    }
}