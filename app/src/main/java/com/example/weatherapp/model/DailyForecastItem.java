package com.example.weatherapp.model;

public class DailyForecastItem {

    private final String dayLabel;
    private final double minTemperature;
    private final double maxTemperature;
    private final String icon;
    private final String description;

    public DailyForecastItem(String dayLabel, double minTemperature, double maxTemperature, String icon, String description) {
        this.dayLabel = dayLabel;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.icon = icon;
        this.description = description;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }
}
