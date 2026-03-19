package com.example.weatherapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.R;
import com.example.weatherapp.model.HourlyForecastItem;

import java.util.ArrayList;
import java.util.List;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder> {

    private final List<HourlyForecastItem> items = new ArrayList<>();

    public void submitItems(List<HourlyForecastItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hourly_forecast, parent, false);
        return new HourlyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {
        HourlyForecastItem item = items.get(position);
        holder.tvHour.setText(item.getTime());
        holder.tvTemp.setText(holder.itemView.getContext().getString(R.string.short_temperature_format, item.getTemperature()));
        bindIcon(holder.ivIcon, item.getIcon());
        
        if (item.getPop() > 0) {
            holder.tvPop.setVisibility(View.VISIBLE);
            holder.tvPop.setText(holder.itemView.getContext().getString(R.string.pop_format, (int) (item.getPop() * 100)));
        } else {
            holder.tvPop.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void bindIcon(ImageView imageView, String iconCode) {
        if (iconCode == null || iconCode.trim().isEmpty()) {
            imageView.setImageDrawable(null);
            return;
        }
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        Glide.with(imageView)
                .load(iconUrl)
                .into(imageView);
    }

    static class HourlyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvHour;
        private final TextView tvTemp;
        private final ImageView ivIcon;
        private final TextView tvPop;

        HourlyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHour = itemView.findViewById(R.id.tvHour);
            tvTemp = itemView.findViewById(R.id.tvHourlyTemp);
            ivIcon = itemView.findViewById(R.id.ivHourlyIcon);
            tvPop = itemView.findViewById(R.id.tvHourlyPop);
        }
    }
}
