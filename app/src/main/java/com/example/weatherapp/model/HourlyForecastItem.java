package com.example.weatherapp.model;

/**
 * Represents a single hourly weather data point used by the forecast chart.
 * Implements {@link ChartForecastItem} so it can be rendered by {@code ForecastChartView}.
 */
public class HourlyForecastItem implements ChartForecastItem {

    private final long dt;
    private final String time;
    private final double temperature;
    private final String icon;
    private final String nightIcon;
    private final double pop;
    private final int humidity;
    private final double uvi;
    private final double windSpeed;
    private final double windGust;
    private final int windDeg;

    /**
     * @param dt          Unix timestamp (seconds) for this hourly slot.
     * @param time        Formatted time label, e.g. {@code "14:00"}.
     * @param temperature Temperature in °C.
     * @param icon        Day weather icon code (e.g. {@code "02d"}).
     * @param nightIcon   Night variant of the icon code (e.g. {@code "02n"}).
     * @param pop         Probability of precipitation (0–1).
     * @param humidity    Relative humidity in %.
     * @param uvi         UV index.
     * @param windSpeed   Wind speed in m/s.
     * @param windGust    Wind gust speed in m/s.
     * @param windDeg     Wind direction in degrees (meteorological).
     */
    public HourlyForecastItem(long dt, String time, double temperature, String icon, String nightIcon, double pop, int humidity, double uvi, double windSpeed, double windGust, int windDeg) {
        this.dt = dt;
        this.time = time;
        this.temperature = temperature;
        this.icon = icon;
        this.nightIcon = nightIcon;
        this.pop = pop;
        this.humidity = humidity;
        this.uvi = uvi;
        this.windSpeed = windSpeed;
        this.windGust = windGust;
        this.windDeg = windDeg;
    }

    @Override public long getDt() { return dt; }
    @Override public String getLabel() { return time; }
    @Override public double getMinTemperature() { return temperature; }
    @Override public double getMaxTemperature() { return temperature; }
    @Override public String getIcon() { return icon; }
    @Override public String getNightIcon() { return nightIcon; }
    @Override public double getPop() { return pop; }
    @Override public int getHumidity() { return humidity; }
    @Override public double getUvi() { return uvi; }
    @Override public double getWindSpeed() { return windSpeed; }
    @Override public double getWindGust() { return windGust; }
    @Override public int getWindDeg() { return windDeg; }
    @Override public int getAqi() { return 1; }
}
