package com.example.childcare.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://10.0.2.2:7220/api/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Thêm logging interceptor để debug
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                Log.d(TAG, "OkHttp: " + message);
            });
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            Log.d(TAG, "Retrofit client created with BASE_URL: " + BASE_URL);
        }
        return retrofit;
    }

    public static Retrofit getClientWithAuth(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        Log.d(TAG, "Creating authenticated client");
        Log.d(TAG, "Token exists: " + (token != null));
        if (token != null) {
            Log.d(TAG, "Token preview: " + token.substring(0, Math.min(20, token.length())) + "...");
        }

        // Thêm logging interceptor để debug
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
            Log.d(TAG, "OkHttp: " + message);
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();

                    if (token != null) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                        Log.d(TAG, "Adding Authorization header");
                    } else {
                        Log.w(TAG, "No token available!");
                    }

                    Request request = requestBuilder.build();
                    Log.d(TAG, "Request URL: " + request.url());
                    Log.d(TAG, "Request Method: " + request.method());

                    return chain.proceed(request);
                })
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }
}