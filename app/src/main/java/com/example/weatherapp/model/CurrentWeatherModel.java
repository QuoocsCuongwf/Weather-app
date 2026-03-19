package com.example.weatherapp.model;

public class CurrentWeatherModel {

    private final String cityName;
    private final double temperature;
    private final int humidity;
    private final double windSpeed;
    private final String weatherStatus;
    private final String icon;
    private final double feelsLike;
    private final double uvi;
    private final int pressure;
    private final int visibility;

    public CurrentWeatherModel(String cityName, double temperature, int humidity, double windSpeed, String weatherStatus, String icon, double feelsLike, double uvi, int pressure, int visibility) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.weatherStatus = weatherStatus;
        this.icon = icon;
        this.feelsLike = feelsLike;
        this.uvi = uvi;
        this.pressure = pressure;
        this.visibility = visibility;
    }

    public String getCityName() {
        return cityName;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String getWeatherStatus() {
        return weatherStatus;
    }

    public String getIcon() {
        return icon;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public double getUvi() {
        return uvi;
    }

    public int getPressure() {
        return pressure;
    }

    public int getVisibility() {
        return visibility;
    }
}
