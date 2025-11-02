package com.example.childcare.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.childcare.R;
import com.example.childcare.models.Service;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private Context context;
    private List<Service> services;

    public ServiceAdapter(Context context, List<Service> services) {
        this.context = context;
        this.services = services;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.service_item, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service s = services.get(position);
        holder.tvName.setText(s.getName());

        // Format giá tiền đẹp hơn
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(s.getPrice()) + " VND");

        String imageUrl = s.getImageUrl();
        if (imageUrl != null && imageUrl.contains("localhost")) {
            imageUrl = imageUrl.replace("localhost", "10.0.2.2");
        }

        android.util.Log.d("SERVICE_IMAGE_URL", "Fixed URL: " + imageUrl);

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.imgService);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.example.childcare.ui.ServiceDetailActivity.class);
            intent.putExtra("serviceId", s.getServiceID());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public void updateList(List<Service> newList) {
        this.services = newList;
        notifyDataSetChanged();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgService;
        TextView tvName, tvPrice;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgService = itemView.findViewById(R.id.imgService);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}