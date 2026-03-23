package com.example.weatherapp.model;

public interface ChartForecastItem {
    long getDt();
    String getLabel();
    String getIcon();
    String getNightIcon();
    
    // Conditions
    double getMinTemperature();
    double getMaxTemperature();
    
    // Wind
    double getWindSpeed();
    double getWindGust();
    int getWindDeg();
    
    // Environment
    double getUvi();
    int getAqi();
    double getPop();
    int getHumidity();
}
