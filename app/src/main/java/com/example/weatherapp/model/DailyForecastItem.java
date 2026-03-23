package com.example.weatherapp.model;

/**
 * Represents a single daily weather data point used by the forecast chart.
 * Implements {@link ChartForecastItem} so it can be rendered by {@code ForecastChartView}.
 */
public class DailyForecastItem implements ChartForecastItem {

    private final long dt;
    private final String dayLabel;
    private final double minTemperature;
    private final double maxTemperature;
    private final String icon;
    private final String nightIcon;
    private final String description;
    private final double pop;
    private final double uvi;
    private final double windSpeed;
    private final int windDeg;
    private final double windGust;
    private final int aqi;
    private final int humidity;

    /**
     * @param dt             Unix timestamp (seconds) for this day.
     * @param dayLabel       Short weekday label, e.g. {@code "Mon"}.
     * @param minTemperature Daily minimum temperature in °C.
     * @param maxTemperature Daily maximum temperature in °C.
     * @param icon           Day weather icon code.
     * @param nightIcon      Night variant of the icon code.
     * @param description    Short weather condition label (e.g. {@code "Rain"}).
     * @param pop            Probability of precipitation (0–1).
     * @param uvi            UV index for the day.
     * @param windSpeed      Wind speed in m/s.
     * @param windDeg        Wind direction in degrees.
     * @param windGust       Wind gust speed in m/s.
     * @param aqi            Air Quality Index (1–5).
     * @param humidity       Relative humidity in %.
     */
    public DailyForecastItem(long dt, String dayLabel, double minTemperature, double maxTemperature, 
                             String icon, String nightIcon, String description, double pop,
                             double uvi, double windSpeed, int windDeg, double windGust, int aqi, int humidity) {
        this.dt = dt;
        this.dayLabel = dayLabel;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.icon = icon;
        this.nightIcon = nightIcon;
        this.description = description;
        this.pop = pop;
        this.uvi = uvi;
        this.windSpeed = windSpeed;
        this.windDeg = windDeg;
        this.windGust = windGust;
        this.aqi = aqi;
        this.humidity = humidity;
    }

    @Override public long getDt() { return dt; }
    @Override public String getLabel() { return dayLabel; }
    @Override public double getMinTemperature() { return minTemperature; }
    @Override public double getMaxTemperature() { return maxTemperature; }
    @Override public String getIcon() { return icon; }
    @Override public String getNightIcon() { return nightIcon; }
    public String getDescription() { return description; }
    @Override public double getPop() { return pop; }
    @Override public double getUvi() { return uvi; }
    @Override public double getWindSpeed() { return windSpeed; }
    @Override public int getWindDeg() { return windDeg; }
    @Override public double getWindGust() { return windGust; }
    @Override public int getAqi() { return aqi; }
    @Override public int getHumidity() { return humidity; }
}
