package com.example.weatherapp.model;

/**
 * Immutable snapshot of the current weather conditions for a single location.
 *
 * <p>All temperature values are in Celsius (metric), wind speeds in m/s,
 * visibility in metres, and pressure in hPa, as returned by the
 * OpenWeatherMap One Call API 3.0.</p>
 */
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
    private final double precipitation;
    private final double windGust;
    private final int windDeg;
    private final int aqi;
    private final double dewPoint;
    private final long sunrise;
    private final long sunset;
    private final long moonrise;
    private final long moonset;
    private final double moonPhase;
    private final double minTemp;
    private final double maxTemp;

    public CurrentWeatherModel(
            // Location & condition
            String cityName, double temperature, int humidity, double windSpeed,
            String weatherStatus, String icon,
            // Feels-like & indices
            double feelsLike, double uvi,
            // Atmospheric
            int pressure, int visibility,
            // Precipitation & wind detail
            double precipitation, double windGust, int windDeg,
            // Air quality & dew
            int aqi, double dewPoint,
            // Sun & moon (Unix epoch seconds)
            long sunrise, long sunset, long moonrise, long moonset, double moonPhase,
            // Daily temp range
            double minTemp, double maxTemp) {
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
        this.precipitation = precipitation;
        this.windGust = windGust;
        this.windDeg = windDeg;
        this.aqi = aqi;
        this.dewPoint = dewPoint;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.moonrise = moonrise;
        this.moonset = moonset;
        this.moonPhase = moonPhase;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public String getCityName() { return cityName; }
    public double getTemperature() { return temperature; }
    public int getHumidity() { return humidity; }
    public double getWindSpeed() { return windSpeed; }
    public String getWeatherStatus() { return weatherStatus; }
    public String getIcon() { return icon; }
    public double getFeelsLike() { return feelsLike; }
    public double getUvi() { return uvi; }
    public int getPressure() { return pressure; }
    public int getVisibility() { return visibility; }
    public double getPrecipitation() { return precipitation; }
    public double getWindGust() { return windGust; }
    public double getMinTemp() { return minTemp; }
    public double getMaxTemp() { return maxTemp; }
    public int getWindDeg() { return windDeg; }
    public int getAqi() { return aqi; }
    public double getDewPoint() { return dewPoint; }
    public long getSunrise() { return sunrise; }
    public long getSunset() { return sunset; }
    public long getMoonrise() { return moonrise; }
    public long getMoonset() { return moonset; }
    public double getMoonPhase() { return moonPhase; }
}
