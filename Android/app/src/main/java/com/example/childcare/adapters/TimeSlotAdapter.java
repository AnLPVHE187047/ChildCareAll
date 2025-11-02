package com.example.childcare.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.childcare.R;
import com.example.childcare.models.TimeSlot;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    private List<TimeSlot> timeSlots;
    private OnTimeSlotClickListener listener;
    private int selectedPosition = -1;

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(TimeSlot timeSlot, int position);
    }

    public TimeSlotAdapter(List<TimeSlot> timeSlots, OnTimeSlotClickListener listener) {
        this.timeSlots = timeSlots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlot slot = timeSlots.get(position);
        holder.tvTime.setText(slot.getTime());

        if (!slot.isAvailable()) {
            // Busy time slot
            holder.card.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
            holder.tvTime.setTextColor(Color.parseColor("#9E9E9E"));
            holder.tvStatus.setText("Busy");
            holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
            holder.card.setEnabled(false);
            holder.card.setClickable(false);
        } else if (position == selectedPosition) {
            // Selected time slot
            holder.card.setCardBackgroundColor(Color.parseColor("#6200EA"));
            holder.tvTime.setTextColor(Color.WHITE);
            holder.tvStatus.setText("Selected");
            holder.tvStatus.setTextColor(Color.WHITE);
            holder.card.setEnabled(true);
        } else {
            // Available time slot
            holder.card.setCardBackgroundColor(Color.WHITE);
            holder.tvTime.setTextColor(Color.parseColor("#333333"));
            holder.tvStatus.setText("Available");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.card.setEnabled(true);
        }

        holder.card.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return; // item đã bị xóa

            TimeSlot clickedSlot = timeSlots.get(adapterPos);
            if (clickedSlot.isAvailable()) {
                int previousPosition = selectedPosition;
                selectedPosition = adapterPos;

                // Update UI
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);

                // Callback
                if (listener != null) {
                    listener.onTimeSlotClick(clickedSlot, adapterPos);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    public void updateTimeSlots(List<TimeSlot> newTimeSlots) {
        this.timeSlots = newTimeSlots;
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    public String getSelectedTime() {
        if (selectedPosition >= 0 && selectedPosition < timeSlots.size()) {
            return timeSlots.get(selectedPosition).getTime();
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvTime;
        TextView tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardTimeSlot);
            tvTime = itemView.findViewById(R.id.tvTimeSlot);
            tvStatus = itemView.findViewById(R.id.tvTimeSlotStatus);
        }
    }
}