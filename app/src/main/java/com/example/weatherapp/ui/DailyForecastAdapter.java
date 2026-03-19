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
import com.example.weatherapp.model.DailyForecastItem;

import java.util.ArrayList;
import java.util.List;

public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.DailyViewHolder> {

    private final List<DailyForecastItem> items = new ArrayList<>();

    public void submitItems(List<DailyForecastItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_forecast, parent, false);
        return new DailyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyViewHolder holder, int position) {
        DailyForecastItem item = items.get(position);
        holder.tvDay.setText(item.getDayLabel());
        holder.tvDesc.setText(item.getDescription() == null || item.getDescription().trim().isEmpty()
                ? holder.itemView.getContext().getString(R.string.forecast_description_placeholder)
                : item.getDescription());
        holder.tvTempRange.setText(holder.itemView.getContext().getString(
                R.string.temperature_range_format,
                item.getMinTemperature(),
                item.getMaxTemperature()
        ));
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

    static class DailyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDay;
        private final TextView tvDesc;
        private final TextView tvTempRange;
        private final ImageView ivIcon;
        private final TextView tvPop;

        DailyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvDesc = itemView.findViewById(R.id.tvDailyDesc);
            tvTempRange = itemView.findViewById(R.id.tvDailyTempRange);
            ivIcon = itemView.findViewById(R.id.ivDailyIcon);
            tvPop = itemView.findViewById(R.id.tvDailyPop);
        }
    }
}
