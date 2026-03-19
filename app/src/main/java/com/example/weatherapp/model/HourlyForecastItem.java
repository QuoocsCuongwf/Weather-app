package com.example.weatherapp.model;

public class HourlyForecastItem {

    private final String time;
    private final double temperature;
    private final String icon;

    public HourlyForecastItem(String time, double temperature, String icon) {
        this.time = time;
        this.temperature = temperature;
        this.icon = icon;
    }

    public String getTime() {
        return time;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getIcon() {
        return icon;
    }
}
