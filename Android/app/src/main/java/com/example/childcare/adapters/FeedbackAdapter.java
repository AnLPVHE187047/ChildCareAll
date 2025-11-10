package com.example.childcare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.childcare.R;
import com.example.childcare.models.FeedbackStaff;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {

    private Context context;
    private List<FeedbackStaff> feedbackList;

    public FeedbackAdapter(Context context, List<FeedbackStaff> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feedback, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeedbackStaff feedback = feedbackList.get(position);

        holder.tvUserName.setText(feedback.getUserName());
        holder.tvServiceName.setText(feedback.getServiceName());
        holder.ratingBar.setRating(feedback.getRating());
        holder.tvComment.setText(feedback.getComment() != null && !feedback.getComment().isEmpty()
                ? feedback.getComment()
                : "Không có nhận xét");

        // Format date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(feedback.getCreatedAt());
            holder.tvDate.setText(outputFormat.format(date));
        } catch (Exception e) {
            holder.tvDate.setText(feedback.getCreatedAt());
        }
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvServiceName, tvComment, tvDate;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvDate = itemView.findViewById(R.id.tvDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}