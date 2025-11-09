package com.example.childcare.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.childcare.R;
import com.example.childcare.models.Appointment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private Context context;
    private List<Appointment> appointments;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
        void onFeedbackClick(Appointment appointment);
    }

    public AppointmentAdapter(Context context, List<Appointment> appointments, OnItemClickListener listener) {
        this.context = context;
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment apt = appointments.get(position);

        holder.tvServiceName.setText(apt.getServiceName());
        holder.tvStaffName.setText("Staff: " + (apt.getStaffName() != null ? apt.getStaffName() : "Not assigned"));

        // Format date
        String formattedDate = formatDate(apt.getAppointmentDate());
        holder.tvDate.setText(formattedDate);

        // Format time
        String formattedTime = formatTime(apt.getAppointmentTime());
        holder.tvTime.setText(formattedTime);

        holder.tvAddress.setText(apt.getAddress());

        // Status với màu sắc
        holder.tvStatus.setText(apt.getStatus());
        setStatusColor(holder.tvStatus, apt.getStatus());

        // Hiển thị nút Feedback nếu status là Completed
        if ("Completed".equalsIgnoreCase(apt.getStatus())) {
            holder.btnFeedback.setVisibility(View.VISIBLE);
            if (apt.isFeedbackGiven()) {
                holder.btnFeedback.setText("Đã đánh giá");
                holder.btnFeedback.setEnabled(false);
            } else {
                holder.btnFeedback.setText("⭐ Đánh giá dịch vụ");
                holder.btnFeedback.setEnabled(true);
                holder.btnFeedback.setOnClickListener(v -> listener.onFeedbackClick(apt));
            }
        } else {
            holder.btnFeedback.setVisibility(View.GONE);
        }


        holder.cardView.setOnClickListener(v -> listener.onItemClick(apt));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private String formatTime(String timeStr) {
        try {
            // Time format: "HH:mm:ss" hoặc "HH:mm:ss.SSSSSSS"
            String[] parts = timeStr.split(":");
            if (parts.length >= 2) {
                return parts[0] + ":" + parts[1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeStr;
    }

    private void setStatusColor(TextView tvStatus, String status) {
        switch (status.toLowerCase()) {
            case "pending":
                tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "confirmed":
                tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
                tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
                break;
            case "completed":
                tvStatus.setTextColor(Color.parseColor("#2196F3")); // Blue
                tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                break;
            case "cancelled":
                tvStatus.setTextColor(Color.parseColor("#F44336")); // Red
                tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                break;
            default:
                tvStatus.setTextColor(Color.parseColor("#757575")); // Gray
                break;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvServiceName, tvStaffName, tvDate, tvTime, tvAddress, tvStatus;
        Button btnFeedback;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvStaffName = itemView.findViewById(R.id.tvStaffName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnFeedback = itemView.findViewById(R.id.btnFeedback);
        }
    }
}