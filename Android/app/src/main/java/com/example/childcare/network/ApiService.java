package com.example.childcare.network;

import com.example.childcare.models.Appointment;
import com.example.childcare.models.AppointmentCreateRequest;
import com.example.childcare.models.LoginRequest;
import com.example.childcare.models.RegisterRequest;
import com.example.childcare.models.Service;
import com.example.childcare.models.Staff;
import com.example.childcare.models.UserResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
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

    // Chỉnh sửa để hỗ trợ filter
    @GET("appointments/my-appointments")
    Call<List<Appointment>> getMyAppointments(
            @Query("userName") String userName,
            @Query("month") Integer month,
            @Query("week") Integer week,
            @Query("status") String status
    );

    @GET("appointments/{id}")
    Call<Appointment> getAppointmentById(@Path("id") int id);

    @GET("users/{id}")
    Call<UserResponse> getUserById(@Path("id") int id);

    @Multipart
    @PUT("users/{id}")
    Call<Void> updateUserProfile(
            @Path("id") int id,
            @Part("FullName") RequestBody fullName,
            @Part("Email") RequestBody email,
            @Part("Phone") RequestBody phone,
            @Part("IsActive") RequestBody isActive,
            @Part MultipartBody.Part imageFile
    );
    @PUT("appointments/{id}/cancel")
    Call<Void> cancelAppointment(@Path("id") int id);
}
