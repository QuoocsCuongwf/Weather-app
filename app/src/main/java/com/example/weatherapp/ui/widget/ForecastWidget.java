package com.example.weatherapp.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.model.ChartForecastItem;

import java.util.List;

public class ForecastWidget extends FrameLayout {

    private TextView tvForecastTitle;
    private ForecastChartView forecastChartView;
    private TextView[] tabs;
    
    private boolean isDaily = true;

    public ForecastWidget(Context context) {
        super(context);
        init(null);
    }

    public ForecastWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_forecast_widget, this, true);

        tvForecastTitle = findViewById(R.id.tvForecastTitle);
        forecastChartView = findViewById(R.id.forecastChartView);
        
        TextView tabConditions = findViewById(R.id.tabConditions);
        TextView tabPrecipitation = findViewById(R.id.tabPrecipitation);
        TextView tabWind = findViewById(R.id.tabWind);
        TextView tabUvIndex = findViewById(R.id.tabUvIndex);
        TextView tabHumidity = findViewById(R.id.tabHumidity);
        
        tabs = new TextView[]{tabConditions, tabPrecipitation, tabWind, tabUvIndex, tabHumidity};

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ForecastWidget);
            String title = a.getString(R.styleable.ForecastWidget_forecastTitle);
            if (title != null) tvForecastTitle.setText(title);
            a.recycle();
        }

        OnClickListener tabClickListener = v -> {
            resetTabs();
            v.setBackgroundResource(R.drawable.bg_tab_selected);
            ((TextView) v).setTextColor(ContextCompat.getColor(getContext(), R.color.weather_text_primary));
            ((TextView) v).setTypeface(null, android.graphics.Typeface.BOLD);

            if (v.getId() == R.id.tabConditions) {
                forecastChartView.setChartMode(ForecastChartView.ChartMode.CONDITIONS);
            } else if (v.getId() == R.id.tabPrecipitation) {
                forecastChartView.setChartMode(ForecastChartView.ChartMode.PRECIPITATION);
            } else if (v.getId() == R.id.tabWind) {
                forecastChartView.setChartMode(ForecastChartView.ChartMode.WIND);
            } else if (v.getId() == R.id.tabUvIndex) {
                forecastChartView.setChartMode(ForecastChartView.ChartMode.UV_INDEX);
            } else if (v.getId() == R.id.tabHumidity) {
                forecastChartView.setChartMode(ForecastChartView.ChartMode.HUMIDITY);
            }
        };

        for (TextView tab : tabs) {
            tab.setOnClickListener(tabClickListener);
        }
    }

    private void resetTabs() {
        for (TextView tab : tabs) {
            tab.setBackgroundResource(R.drawable.bg_tab_unselected);
            tab.setTextColor(ContextCompat.getColor(getContext(), R.color.weather_text_secondary));
            tab.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    public void setForecastData(List<? extends ChartForecastItem> items, boolean isDaily) {
        this.isDaily = isDaily;
        forecastChartView.setForecastData(items, isDaily);
    }
}
