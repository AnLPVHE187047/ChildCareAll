package com.example.childcare.network;

import com.example.childcare.models.Appointment;
import com.example.childcare.models.AppointmentCreateRequest;
import com.example.childcare.models.LoginRequest;
import com.example.childcare.models.RegisterRequest;
import com.example.childcare.models.Service;
import com.example.childcare.models.Staff;
import com.example.childcare.models.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("auth/login")
    Call<UserResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<UserResponse> register(@Body RegisterRequest request);

    @GET("services")
    Call<List<Service>> getAllServices();

    @GET("services/{id}")
    Call<Service> getServiceById(@Path("id") int id);

    @GET("staffs")
    Call<List<Staff>> getAllStaffs();

    @POST("appointments")
    Call<Appointment> createAppointment(@Body AppointmentCreateRequest request);

    @GET("staffs/{staffId}/schedule")
    Call<List<Appointment>> getStaffSchedule(
            @Path("staffId") int staffId,
            @Query("date") String date
    );

    // Lấy danh sách appointments của user hiện tại
    @GET("appointments/my-appointments")
    Call<List<Appointment>> getMyAppointments();

    // Lấy chi tiết appointment theo ID
    @GET("appointments/{id}")
    Call<Appointment> getAppointmentById(@Path("id") int id);
}