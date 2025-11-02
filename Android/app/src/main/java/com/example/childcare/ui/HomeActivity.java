package com.example.childcare.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import android.view.View;

import com.example.childcare.R;
import com.example.childcare.models.Service;
import com.example.childcare.adapters.ServiceAdapter;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView btnMenu;
    private TextView tvGreeting, navHome, navProfile, navSchedule, navLogout;

    private RecyclerView rvServices;
    private EditText edtSearch;
    private Spinner spinnerPriceFilter;
    private Button btnApplyFilter, btnClearFilter;

    private List<Service> serviceList = new ArrayList<>();
    private List<Service> filteredList = new ArrayList<>();
    private ServiceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 🔹 Ánh xạ view
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        tvGreeting = findViewById(R.id.tvGreeting);
        navHome = findViewById(R.id.navHome);
        navProfile = findViewById(R.id.navProfile);
        navSchedule = findViewById(R.id.navSchedule);
        navLogout = findViewById(R.id.navLogout);

        rvServices = findViewById(R.id.rvServices);
        edtSearch = findViewById(R.id.edtSearch);
        spinnerPriceFilter = findViewById(R.id.spinnerPriceFilter);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnClearFilter = findViewById(R.id.btnClearFilter);

        // 🔹 Cấu hình RecyclerView
        rvServices.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ServiceAdapter(this, filteredList);
        rvServices.setAdapter(adapter);

        // 🔹 Khởi tạo giao diện
        setupPriceSpinner();
        setupFilterButtons();
        setupSearch();
        setupDrawerMenu();
        loadServices();

        // 🔹 Chào người dùng
        String fullName = getIntent().getStringExtra("fullName");
        if (fullName != null) {
            tvGreeting.setText("Xin chào, " + fullName + "!");
        }

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    // 🔸 Drawer menu navigation
    private void setupDrawerMenu() {
        navHome.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        navProfile.setOnClickListener(v -> {
            // startActivity(new Intent(this, ProfileActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        navSchedule.setOnClickListener(v -> {
            startActivity(new Intent(this, AppointmentHistoryActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        navLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            prefs.edit().remove("token").apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // 🔸 Spinner giá
    private void setupPriceSpinner() {
        String[] priceRanges = {
                "All Prices",
                "Under 200,000 VND",
                "200,000 - 300,000 VND",
                "300,000 - 500,000 VND",
                "500,000 - 1,000,000 VND",
                "Above 1,000,000 VND"
        };

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                priceRanges
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriceFilter.setAdapter(spinnerAdapter);
    }

    // 🔸 Nút Filter
    private void setupFilterButtons() {
        btnApplyFilter.setOnClickListener(v -> applyFilters());
        btnClearFilter.setOnClickListener(v -> clearFilters());
    }

    // 🔸 Thanh tìm kiếm
    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
        });
    }

    // 🔸 Áp dụng tìm kiếm + lọc
    private void applyFilters() {
        String keyword = edtSearch.getText().toString().toLowerCase().trim();
        int priceFilterPosition = spinnerPriceFilter.getSelectedItemPosition();

        filteredList.clear();

        for (Service service : serviceList) {
            boolean matchesKeyword = keyword.isEmpty() ||
                    service.getName().toLowerCase().contains(keyword);
            boolean matchesPrice = filterByPrice(service.getPrice(), priceFilterPosition);

            if (matchesKeyword && matchesPrice) {
                filteredList.add(service);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No services found", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean filterByPrice(double price, int filterPosition) {
        switch (filterPosition) {
            case 1: return price < 200000;
            case 2: return price >= 200000 && price <= 300000;
            case 3: return price >= 300000 && price <= 500000;
            case 4: return price >= 500000 && price <= 1000000;
            case 5: return price > 1000000;
            default: return true;
        }
    }

    private void clearFilters() {
        edtSearch.setText("");
        spinnerPriceFilter.setSelection(0);
        filteredList.clear();
        filteredList.addAll(serviceList);
        adapter.notifyDataSetChanged();
    }

    // 🔸 Load dữ liệu Service
    private void loadServices() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getAllServices().enqueue(new Callback<List<Service>>() {
            @Override
            public void onResponse(Call<List<Service>> call, Response<List<Service>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    serviceList.clear();
                    serviceList.addAll(response.body());

                    filteredList.clear();
                    filteredList.addAll(serviceList);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load services", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Service>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
