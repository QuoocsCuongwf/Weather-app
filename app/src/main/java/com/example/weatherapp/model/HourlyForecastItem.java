package com.example.weatherapp.model;

public class HourlyForecastItem {

    private final String time;
    private final double temperature;
    private final String icon;
    private final double pop;

    public HourlyForecastItem(String time, double temperature, String icon, double pop) {
        this.time = time;
        this.temperature = temperature;
        this.icon = icon;
        this.pop = pop;
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

    public double getPop() {
        return pop;
    }
}
