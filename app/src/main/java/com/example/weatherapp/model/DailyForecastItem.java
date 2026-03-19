package com.example.weatherapp.model;

public class DailyForecastItem {

    private final String dayLabel;
    private final double minTemperature;
    private final double maxTemperature;
    private final String icon;
    private final String description;
    private final double pop;

    public DailyForecastItem(String dayLabel, double minTemperature, double maxTemperature, String icon, String description, double pop) {
        this.dayLabel = dayLabel;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.icon = icon;
        this.description = description;
        this.pop = pop;
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

    public double getPop() {
        return pop;
    }
}
