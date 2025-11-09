package com.example.childcare.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.childcare.R;
import com.example.childcare.adapters.TimeSlotAdapter;
import com.example.childcare.models.*;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import retrofit2.*;

public class BookAppointmentActivity extends AppCompatActivity {

    private static final String TAG = "BookAppointment";

    private MaterialAutoCompleteTextView actvStaff;
    private TextInputEditText etDate, etAddress;
    private MaterialButton btnSubmit;
    private RecyclerView rvTimeSlots;
    private TextView tvServiceName, tvServiceDuration, tvTimeSlotInfo;
    private View loadingOverlay;

    private List<Staff> staffList = new ArrayList<>();
    private TimeSlotAdapter timeSlotAdapter;
    private List<TimeSlot> timeSlots = new ArrayList<>();

    private int selectedStaffId = -1;
    private int serviceId;
    private int serviceDuration = 60;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        initViews();
        setupToolbar();

        serviceId = getIntent().getIntExtra("serviceId", -1);
        String serviceName = getIntent().getStringExtra("serviceName");
        serviceDuration = getIntent().getIntExtra("serviceDuration", 60);

        tvServiceName.setText(serviceName != null ? serviceName : "Service");
        tvServiceDuration.setText("Thời lượng: " + serviceDuration + " phút");

        loadStaffs();
        setupTimeSlotGrid();
        setupListeners();
    }

    private void initViews() {
        actvStaff = findViewById(R.id.actvStaff);
        etDate = findViewById(R.id.etDate);
        etAddress = findViewById(R.id.etAddress);
        btnSubmit = findViewById(R.id.btnSubmit);
        rvTimeSlots = findViewById(R.id.rvTimeSlots);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvServiceDuration = findViewById(R.id.tvServiceDuration);
        tvTimeSlotInfo = findViewById(R.id.tvTimeSlotInfo);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupToolbar() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private void setupTimeSlotGrid() {
        rvTimeSlots.setLayoutManager(new GridLayoutManager(this, 3));
        timeSlotAdapter = new TimeSlotAdapter(timeSlots, (slot, position) -> {
            // Time slot selected
        });
        rvTimeSlots.setAdapter(timeSlotAdapter);
    }

    private void setupListeners() {
        etDate.setOnClickListener(v -> showDatePicker());
        btnSubmit.setOnClickListener(v -> submitBooking());
    }

    private void loadStaffs() {
        showLoading(true);

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getAllStaffs().enqueue(new Callback<List<Staff>>() {
            @Override
            public void onResponse(Call<List<Staff>> call, Response<List<Staff>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    staffList = response.body();
                    Log.d(TAG, "Loaded " + staffList.size() + " staff members");

                    if (staffList.isEmpty()) {
                        Toast.makeText(BookAppointmentActivity.this,
                                "Không có nhân viên nào", Toast.LENGTH_SHORT).show();
                    } else {
                        setupStaffDropdown();
                    }
                } else {
                    Log.e(TAG, "Failed to load staff: " + response.code());
                    Toast.makeText(BookAppointmentActivity.this,
                            "Lỗi tải danh sách nhân viên: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Staff>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading staff", t);
                Toast.makeText(BookAppointmentActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupStaffDropdown() {
        List<String> staffNames = new ArrayList<>();
        for (Staff s : staffList) {
            staffNames.add(s.getFullName());
            Log.d(TAG, "Staff: " + s.getFullName() + " (ID: " + s.getStaffID() + ")");
        }

        // CRITICAL FIX: Use simple_list_item_1 instead of simple_dropdown_item_1line
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                staffNames
        );

        actvStaff.setAdapter(adapter);

        // Make sure the dropdown shows on click
        actvStaff.setOnClickListener(v -> {
            actvStaff.showDropDown();
        });

        actvStaff.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                actvStaff.showDropDown();
            }
        });

        actvStaff.setOnItemClickListener((parent, view, position, id) -> {
            selectedStaffId = staffList.get(position).getStaffID();
            Log.d(TAG, "Selected staff ID: " + selectedStaffId);

            // Hide keyboard
            actvStaff.clearFocus();

            loadAvailableTimeSlots();
        });

        Log.d(TAG, "Staff dropdown setup completed with " + staffNames.size() + " items");
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();

        // Set minimum date to today
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                    etDate.setText(selectedDate);
                    Log.d(TAG, "Selected date: " + selectedDate);
                    loadAvailableTimeSlots();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        Calendar minCal = Calendar.getInstance();
        minCal.add(Calendar.HOUR_OF_DAY, 12);
        datePickerDialog.getDatePicker().setMinDate(minCal.getTimeInMillis());
        datePickerDialog.show();
    }

    private void loadAvailableTimeSlots() {
        if (selectedStaffId == -1 || selectedDate.isEmpty()) {
            tvTimeSlotInfo.setVisibility(View.VISIBLE);
            rvTimeSlots.setVisibility(View.GONE);
            tvTimeSlotInfo.setText("Vui lòng chọn nhân viên và ngày trước");
            return;
        }

        showLoading(true);
        Log.d(TAG, "Loading time slots for staff " + selectedStaffId + " on " + selectedDate);

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getStaffSchedule(selectedStaffId, selectedDate).enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Appointment> busyAppointments = response.body();
                    Log.d(TAG, "Staff has " + busyAppointments.size() + " busy appointments");
                    generateTimeSlots(busyAppointments);
                } else {
                    Log.w(TAG, "No appointments or error: " + response.code());
                    generateTimeSlots(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to load schedule", t);
                Toast.makeText(BookAppointmentActivity.this,
                        "Lỗi tải lịch: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                generateTimeSlots(new ArrayList<>());
            }
        });
    }

    private void generateTimeSlots(List<Appointment> busyAppointments) {
        timeSlots.clear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

        Calendar now = Calendar.getInstance();
        Calendar minAllowed = Calendar.getInstance();
        minAllowed.add(Calendar.HOUR_OF_DAY, 12);

        while (calendar.get(Calendar.HOUR_OF_DAY) < 18) {
            String timeSlot = timeFormat.format(calendar.getTime());

            // Nếu người dùng chọn ngày hôm nay, bỏ qua các giờ < 12 tiếng tới
            Calendar selectedCal = Calendar.getInstance();
            try {
                selectedCal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate));
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean skip = false;
            if (isSameDay(selectedCal, now) && calendar.before(minAllowed)) {
                skip = true; // Bỏ qua khung giờ này
            }

            if (!skip) {
                boolean isAvailable = !isTimeSlotBusy(timeSlot, busyAppointments);
                timeSlots.add(new TimeSlot(timeSlot, isAvailable));
            }

            calendar.add(Calendar.MINUTE, 30);
        }


        timeSlotAdapter.updateTimeSlots(timeSlots);

        tvTimeSlotInfo.setVisibility(View.GONE);
        rvTimeSlots.setVisibility(View.VISIBLE);

        Log.d(TAG, "Generated " + timeSlots.size() + " time slots");
    }
    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isTimeSlotBusy(String checkTime, List<Appointment> busyAppointments) {
        for (Appointment apt : busyAppointments) {
            String startTime = apt.getAppointmentTime();

            String[] parts = startTime.split(":");
            if (parts.length < 2) continue;

            int startHour = Integer.parseInt(parts[0]);
            int startMin = Integer.parseInt(parts[1]);

            Calendar start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY, startHour);
            start.set(Calendar.MINUTE, startMin);

            Calendar end = (Calendar) start.clone();
            end.add(Calendar.MINUTE, serviceDuration);

            String[] checkParts = checkTime.split(":");
            int checkHour = Integer.parseInt(checkParts[0]);
            int checkMin = Integer.parseInt(checkParts[1]);

            Calendar check = Calendar.getInstance();
            check.set(Calendar.HOUR_OF_DAY, checkHour);
            check.set(Calendar.MINUTE, checkMin);

            Calendar checkEnd = (Calendar) check.clone();
            checkEnd.add(Calendar.MINUTE, serviceDuration);

            boolean overlap = check.before(end) && checkEnd.after(start);
            if (overlap) return true;
        }
        return false;
    }

    private void submitBooking() {
        String selectedTime = timeSlotAdapter.getSelectedTime();
        String address = etAddress.getText().toString().trim();

        if (selectedStaffId == -1) {
            Toast.makeText(this, "Vui lòng chọn nhân viên", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTime == null) {
            Toast.makeText(this, "Vui lòng chọn giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        btnSubmit.setEnabled(false);

        AppointmentCreateRequest req = new AppointmentCreateRequest(
                serviceId, selectedStaffId, selectedDate, selectedTime, address
        );

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.createAppointment(req).enqueue(new Callback<Appointment>() {
            @Override
            public void onResponse(Call<Appointment> call, Response<Appointment> response) {
                showLoading(false);
                btnSubmit.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Appointment appointment = response.body();
                    showSuccessDialogAndNavigate(appointment);
                } else {
                    String errorMessage = parseErrorMessage(response);
                    showErrorDialog("Đặt lịch thất bại", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Appointment> call, Throwable t) {
                showLoading(false);
                btnSubmit.setEnabled(true);
                showErrorDialog("Lỗi kết nối", "❌ " + t.getMessage());
            }
        });
    }

    private String parseErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                Gson gson = new Gson();
                ErrorResponse errorResponse = gson.fromJson(errorJson, ErrorResponse.class);

                if (errorResponse != null && errorResponse.getMessage() != null) {
                    return errorResponse.getMessage();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        switch (response.code()) {
            case 409: return "Nhân viên đã bận trong khung giờ này.\nVui lòng chọn giờ khác.";
            case 400: return "Dữ liệu không hợp lệ.\nVui lòng kiểm tra lại.";
            case 401: return "Chưa đăng nhập.\nVui lòng đăng nhập lại.";
            case 404: return "Không tìm thấy dịch vụ hoặc nhân viên.";
            case 500: return "Lỗi máy chủ.\nVui lòng thử lại sau.";
            default: return "Đặt lịch thất bại (Lỗi " + response.code() + ")";
        }
    }

    private void showSuccessDialogAndNavigate(Appointment appointment) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("✅ Đặt lịch thành công!")
                .setMessage(
                        "Dịch vụ: " + appointment.getServiceName() + "\n" +
                                "Nhân viên: " + appointment.getStaffName() + "\n" +
                                "Ngày: " + appointment.getAppointmentDate() + "\n" +
                                "Giờ: " + appointment.getAppointmentTime() + "\n" +
                                "Trạng thái: " + appointment.getStatus()
                )
                .setPositiveButton("Xem lịch hẹn", (dialog, which) -> {
                    Intent intent = new Intent(BookAppointmentActivity.this, AppointmentHistoryActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Về trang chủ", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}