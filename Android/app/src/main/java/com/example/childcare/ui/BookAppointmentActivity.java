package com.example.childcare.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
        tvServiceDuration.setText("Duration: " + serviceDuration + " minutes");

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
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getAllStaffs().enqueue(new Callback<List<Staff>>() {
            @Override
            public void onResponse(Call<List<Staff>> call, Response<List<Staff>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    staffList = response.body();
                    setupStaffDropdown();
                }
            }

            @Override
            public void onFailure(Call<List<Staff>> call, Throwable t) {
                Toast.makeText(BookAppointmentActivity.this,
                        "Failed to load staff: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupStaffDropdown() {
        List<String> staffNames = new ArrayList<>();
        for (Staff s : staffList) {
            staffNames.add(s.getFullName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, staffNames);
        actvStaff.setAdapter(adapter);

        actvStaff.setOnItemClickListener((parent, view, position, id) -> {
            selectedStaffId = staffList.get(position).getStaffID();
            loadAvailableTimeSlots();
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            etDate.setText(selectedDate);
            loadAvailableTimeSlots();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadAvailableTimeSlots() {
        if (selectedStaffId == -1 || selectedDate.isEmpty()) {
            tvTimeSlotInfo.setVisibility(View.VISIBLE);
            rvTimeSlots.setVisibility(View.GONE);
            tvTimeSlotInfo.setText("Please select staff and date first");
            return;
        }

        showLoading(true);

        ApiService api = ApiClient.getClientWithAuth(this).create(ApiService.class);
        api.getStaffSchedule(selectedStaffId, selectedDate).enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Appointment> busyAppointments = response.body();
                    generateTimeSlots(busyAppointments);
                } else {
                    generateTimeSlots(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                showLoading(false);
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

        while (calendar.get(Calendar.HOUR_OF_DAY) < 18) {
            String timeSlot = timeFormat.format(calendar.getTime());
            boolean isAvailable = !isTimeSlotBusy(timeSlot, busyAppointments);
            timeSlots.add(new TimeSlot(timeSlot, isAvailable));

            calendar.add(Calendar.MINUTE, 30);
        }

        timeSlotAdapter.updateTimeSlots(timeSlots);

        tvTimeSlotInfo.setVisibility(View.GONE);
        rvTimeSlots.setVisibility(View.VISIBLE);
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
            Toast.makeText(this, "Please select a staff member", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTime == null) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            return;
        }

        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show();
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
                    showErrorDialog("Booking Failed", errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Appointment> call, Throwable t) {
                showLoading(false);
                btnSubmit.setEnabled(true);
                showErrorDialog("Connection Failed", "❌ " + t.getMessage());
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
            case 409: return "Staff is already busy during this time slot.\nPlease choose another time.";
            case 400: return "Invalid appointment data.\nPlease check your input.";
            case 401: return "Unauthorized.\nPlease login again.";
            case 404: return "Service or Staff not found.";
            case 500: return "Server error.\nPlease try again later.";
            default: return "Booking failed (Error " + response.code() + ")";
        }
    }

    private void showSuccessDialogAndNavigate(Appointment appointment) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("✅ Booking Successful!")
                .setMessage(
                        "Service: " + appointment.getServiceName() + "\n" +
                                "Staff: " + appointment.getStaffName() + "\n" +
                                "Date: " + appointment.getAppointmentDate() + "\n" +
                                "Time: " + appointment.getAppointmentTime() + "\n" +
                                "Status: " + appointment.getStatus()
                )
                .setPositiveButton("View My Appointments", (dialog, which) -> {
                    // Chuyển sang trang lịch sử
                    Intent intent = new Intent(BookAppointmentActivity.this, AppointmentHistoryActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Back to Home", (dialog, which) -> {
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