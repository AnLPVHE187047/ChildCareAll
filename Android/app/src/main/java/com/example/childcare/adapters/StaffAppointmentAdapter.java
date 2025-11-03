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
    public String formatDate(String dateStr) {
        try {
            // Giả sử API trả về ngày theo định dạng ISO: "yyyy-MM-dd'T'HH:mm:ss"
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.US);
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr; // Trả về nguyên bản nếu lỗi
        }
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment a = appointments.get(position);

        holder.tvDate.setText(formatDate(a.getAppointmentDate()));
        holder.tvCustomer.setText("Khách: " + a.getUserName());
        holder.tvService.setText("Dịch vụ: " + a.getServiceName());
        holder.tvStatus.setText("Trạng thái: " + a.getStatus());

        // Set status color
        switch (a.getStatus().toLowerCase()) {
            case "pending":
                holder.tvStatus.setTextColor(Color.parseColor("#FFA000"));
                break;
            case "confirmed":
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case "completed":
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
                break;
            case "cancelled":
                holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
                break;
            default:
                holder.tvStatus.setTextColor(Color.parseColor("#757575"));
                break;
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(a);
            }
        });
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

