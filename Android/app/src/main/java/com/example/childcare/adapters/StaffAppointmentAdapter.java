package com.example.childcare.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childcare.R;
import com.example.childcare.models.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffAppointmentAdapter extends RecyclerView.Adapter<StaffAppointmentAdapter.ViewHolder> {
    private Context context;
    private List<Appointment> appointments;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
        void onChangeStatus(Appointment appointment, String newStatus);
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

    /**
     * Kiểm tra xem appointment date có trước hôm nay không
     */
    private boolean isBeforeToday(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date appointmentDate = sdf.parse(dateStr);

            // Lấy ngày hiện tại (bỏ phần giờ)
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar appointmentCal = Calendar.getInstance();
            appointmentCal.setTime(appointmentDate);
            appointmentCal.set(Calendar.HOUR_OF_DAY, 0);
            appointmentCal.set(Calendar.MINUTE, 0);
            appointmentCal.set(Calendar.SECOND, 0);
            appointmentCal.set(Calendar.MILLISECOND, 0);

            return appointmentCal.before(today);
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Kiểm tra xem appointment date có sau hoặc bằng hôm nay không
     */
    private boolean isAfterOrEqualsToday(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date appointmentDate = sdf.parse(dateStr);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar appointmentCal = Calendar.getInstance();
            appointmentCal.setTime(appointmentDate);
            appointmentCal.set(Calendar.HOUR_OF_DAY, 0);
            appointmentCal.set(Calendar.MINUTE, 0);
            appointmentCal.set(Calendar.SECOND, 0);
            appointmentCal.set(Calendar.MILLISECOND, 0);

            return !appointmentCal.before(today);
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment a = appointments.get(position);
        holder.tvDate.setText(formatDate(a.getAppointmentDate()) + " " + formatTime(a.getAppointmentTime()));
        holder.tvCustomer.setText("Khách: " + a.getUserName());
        holder.tvService.setText("Dịch vụ: " + a.getServiceName());
        holder.tvStatus.setText("Trạng thái: " + translateStatus(a.getStatus()));

        // Màu theo trạng thái
        switch (a.getStatus()) {
            case "Completed":
                holder.itemView.setBackgroundColor(Color.parseColor("#E8F5E9")); // xanh lá nhạt
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                break;
            case "Confirmed":
                holder.itemView.setBackgroundColor(Color.parseColor("#E3F2FD")); // xanh dương nhạt
                holder.tvStatus.setTextColor(Color.parseColor("#1976D2"));
                break;
            case "Cancelled":
                holder.itemView.setBackgroundColor(Color.parseColor("#FFEBEE")); // đỏ nhạt
                holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
                break;
            default: // Pending
                if (isToday(a.getAppointmentDate())) {
                    holder.itemView.setBackgroundColor(Color.parseColor("#FFF9C4")); // vàng nhạt
                } else {
                    holder.itemView.setBackgroundColor(Color.WHITE);
                }
                holder.tvStatus.setTextColor(Color.parseColor("#F57C00"));
        }

        // Logic hiển thị button theo business rules
        setupButtons(holder, a);

        // Setup click listeners
        holder.btnConfirm.setOnClickListener(v -> {
            if (listener != null) listener.onChangeStatus(a, "Confirmed");
        });

        holder.btnComplete.setOnClickListener(v -> {
            if (listener != null) listener.onChangeStatus(a, "Completed");
        });

        holder.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onChangeStatus(a, "Cancelled");
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(a);
        });
    }

    /**
     * Setup buttons theo business rules:
     * 1. Confirmed: chỉ hiện khi Pending VÀ trước ngày đặt lịch
     * 2. Completed: chỉ hiện khi Confirmed VÀ sau/bằng ngày đặt lịch
     * 3. Cancel: hiện khi chưa Completed (nhưng có thể thêm logic khác)
     */
    private void setupButtons(ViewHolder holder, Appointment a) {
        // Ẩn tất cả button mặc định
        holder.btnConfirm.setVisibility(View.GONE);
        holder.btnComplete.setVisibility(View.GONE);
        holder.btnCancel.setVisibility(View.GONE);

        String status = a.getStatus();
        boolean isBeforeAppointmentDate = isAfterOrEqualsToday(a.getAppointmentDate());
        boolean isAfterOrEqualAppointmentDate = isAfterOrEqualsToday(a.getAppointmentDate());

        if ("Pending".equalsIgnoreCase(status)) {
            // Pending: chỉ có thể Confirm nếu trước ngày đặt lịch
            if (isBeforeAppointmentDate) {
                holder.btnConfirm.setVisibility(View.VISIBLE);
            }
            // Luôn có thể cancel
            holder.btnCancel.setVisibility(View.VISIBLE);

        } else if ("Confirmed".equalsIgnoreCase(status)) {
            // Confirmed: chỉ có thể Complete nếu sau/bằng ngày đặt lịch
            if (isAfterOrEqualAppointmentDate) {
                holder.btnComplete.setVisibility(View.VISIBLE);
            }
            // Có thể cancel (nếu chưa đến ngày)
            holder.btnCancel.setVisibility(View.VISIBLE);

        } else if ("Completed".equalsIgnoreCase(status)) {
            // Completed: không hiện button nào (đã hoàn tất)

        } else if ("Cancelled".equalsIgnoreCase(status)) {
            // Cancelled: không hiện button nào
        }
    }

    private String translateStatus(String status) {
        switch (status) {
            case "Pending": return "Chờ xác nhận";
            case "Confirmed": return "Đã xác nhận";
            case "Completed": return "Đã hoàn tất";
            case "Cancelled": return "Đã hủy";
            default: return status;
        }
    }

    private boolean isToday(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = sdf.parse(dateStr);
            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
            return dayFormat.format(date).equals(dayFormat.format(new Date()));
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvCustomer, tvService, tvStatus;
        Button btnConfirm, btnComplete, btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvService = itemView.findViewById(R.id.tvService);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnComplete = itemView.findViewById(R.id.btnComplete);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}