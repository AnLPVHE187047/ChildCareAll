package com.example.childcare.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childcare.R;
import com.example.childcare.models.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffAppointmentAdapter extends RecyclerView.Adapter<StaffAppointmentAdapter.ViewHolder> {
    private Context context;
    private List<Appointment> appointments;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public StaffAppointmentAdapter(Context context, List<Appointment> appointments) {
        this.context = context;
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment_card, parent, false);
        return new ViewHolder(view);
    }
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.US);
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String formatTime(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            if (parts.length >= 2) {
                int hour = Integer.parseInt(parts[0]);
                int min = Integer.parseInt(parts[1]);
                return String.format(Locale.US, "%02d:%02d", hour, min);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeStr;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment a = appointments.get(position);
        holder.tvDate.setText(formatDate(a.getAppointmentDate()) + " " + formatTime(a.getAppointmentTime()));
        holder.tvCustomer.setText("Khách: " + a.getUserName());
        holder.tvService.setText("Dịch vụ: " + a.getServiceName());
        holder.tvStatus.setText("Trạng thái: " + a.getStatus());

        if (isNearNow(a.getAppointmentDate(), a.getAppointmentTime())) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFF9C4")); // nền vàng nhạt
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(a);
        });
    }

    private boolean isNearNow(String dateStr, String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date appointmentDate = sdf.parse(dateStr + "T" + timeStr + ":00"); // tạo Date full
            long diff = appointmentDate.getTime() - new Date().getTime();
            return diff >= 0 && diff <= 3600 * 1000; // trong 1 giờ tới
        } catch (Exception e) {
            return false;
        }
    }



    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvCustomer, tvService, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvService = itemView.findViewById(R.id.tvService);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}

