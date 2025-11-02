package com.example.childcare.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.childcare.R;
import com.example.childcare.models.Service;
import com.example.childcare.network.ApiClient;
import com.example.childcare.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.appcompat.widget.Toolbar;

public class ServiceDetailActivity extends AppCompatActivity {

    private ImageView imgServiceDetail;
    private TextView tvServiceName, tvServiceDescription, tvServicePrice, tvServiceDuration;
    private Button btnBookNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Service Details");
        }

        imgServiceDetail = findViewById(R.id.imgServiceDetail);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvServiceDescription = findViewById(R.id.tvServiceDescription);
        tvServicePrice = findViewById(R.id.tvServicePrice);
        tvServiceDuration = findViewById(R.id.tvServiceDuration);
        btnBookNow = findViewById(R.id.btnBookNow);

        int serviceId = getIntent().getIntExtra("serviceId", -1);
        if (serviceId != -1) {
            loadServiceDetail(serviceId);

            // ✅ Đặt sự kiện ở đây
            btnBookNow.setOnClickListener(v -> {
                Intent intent = new Intent(ServiceDetailActivity.this, BookAppointmentActivity.class);
                intent.putExtra("serviceId", serviceId);
                startActivity(intent);
            });
        } else {
            Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadServiceDetail(int id) {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getServiceById(id).enqueue(new Callback<Service>() {
            @Override
            public void onResponse(Call<Service> call, Response<Service> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Service s = response.body();

                    tvServiceName.setText(s.getName());
                    tvServiceDescription.setText(s.getDescription());
                    tvServicePrice.setText(String.format("%.0f VND", s.getPrice()));
                    tvServiceDuration.setText("Thời lượng: " + s.getDurationMinutes() + " phút");

                    String imageUrl = s.getImageUrl();
                    if (imageUrl != null && imageUrl.contains("localhost")) {
                        imageUrl = imageUrl.replace("localhost", "10.0.2.2");
                    }

                    Glide.with(ServiceDetailActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(imgServiceDetail);
                } else {
                    Toast.makeText(ServiceDetailActivity.this, "Failed to load service detail", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Service> call, Throwable t) {
                Toast.makeText(ServiceDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
