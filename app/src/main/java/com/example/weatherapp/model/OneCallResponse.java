package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OneCallResponse {

    @SerializedName("lat")
    private double lat;

    @SerializedName("lon")
    private double lon;

    @SerializedName("timezone")
    private String timezone;

    @SerializedName("timezone_offset")
    private int timezoneOffset;

    @SerializedName("current")
    private Current current;

    @SerializedName("minutely")
    private List<Minutely> minutely;

    @SerializedName("hourly")
    private List<Hourly> hourly;

    @SerializedName("daily")
    private List<Daily> daily;

    @SerializedName("alerts")
    private List<Alert> alerts;

    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public String getTimezone() { return timezone; }
    public int getTimezoneOffset() { return timezoneOffset; }
    public Current getCurrent() { return current; }
    public List<Minutely> getMinutely() { return minutely; }
    public List<Hourly> getHourly() { return hourly; }
    public List<Daily> getDaily() { return daily; }
    public List<Alert> getAlerts() { return alerts; }

    public static class Current {
        @SerializedName("dt")
        private long dt;
        @SerializedName("sunrise")
        private long sunrise;
        @SerializedName("sunset")
        private long sunset;
        @SerializedName("temp")
        private double temp;
        @SerializedName("feels_like")
        private double feelsLike;
        @SerializedName("pressure")
        private int pressure;
        @SerializedName("humidity")
        private int humidity;
        @SerializedName("dew_point")
        private double dewPoint;
        @SerializedName("uvi")
        private double uvi;
        @SerializedName("clouds")
        private int clouds;
        @SerializedName("visibility")
        private int visibility;
        @SerializedName("wind_speed")
        private double windSpeed;
        @SerializedName("wind_deg")
        private int windDeg;
        @SerializedName("wind_gust")
        private double windGust;
        @SerializedName("weather")
        private List<Weather> weather;

        public long getDt() { return dt; }
        public long getSunrise() { return sunrise; }
        public long getSunset() { return sunset; }
        public double getTemp() { return temp; }
        public double getFeelsLike() { return feelsLike; }
        public int getPressure() { return pressure; }
        public int getHumidity() { return humidity; }
        public double getDewPoint() { return dewPoint; }
        public double getUvi() { return uvi; }
        public int getClouds() { return clouds; }
        public int getVisibility() { return visibility; }
        public double getWindSpeed() { return windSpeed; }
        public int getWindDeg() { return windDeg; }
        public double getWindGust() { return windGust; }
        public List<Weather> getWeather() { return weather; }
    }

    public static class Minutely {
        @SerializedName("dt")
        private long dt;
        @SerializedName("precipitation")
        private double precipitation;

        public long getDt() { return dt; }
        public double getPrecipitation() { return precipitation; }
    }

    public static class Hourly {
        @SerializedName("dt")
        private long dt;
        @SerializedName("temp")
        private double temp;
        @SerializedName("feels_like")
        private double feelsLike;
        @SerializedName("pressure")
        private int pressure;
        @SerializedName("humidity")
        private int humidity;
        @SerializedName("dew_point")
        private double dewPoint;
        @SerializedName("uvi")
        private double uvi;
        @SerializedName("clouds")
        private int clouds;
        @SerializedName("visibility")
        private int visibility;
        @SerializedName("wind_speed")
        private double windSpeed;
        @SerializedName("wind_deg")
        private int windDeg;
        @SerializedName("wind_gust")
        private double windGust;
        @SerializedName("weather")
        private List<Weather> weather;
        @SerializedName("pop")
        private double pop;

        public long getDt() { return dt; }
        public double getTemp() { return temp; }
        public double getFeelsLike() { return feelsLike; }
        public int getPressure() { return pressure; }
        public int getHumidity() { return humidity; }
        public double getDewPoint() { return dewPoint; }
        public double getUvi() { return uvi; }
        public int getClouds() { return clouds; }
        public int getVisibility() { return visibility; }
        public double getWindSpeed() { return windSpeed; }
        public int getWindDeg() { return windDeg; }
        public double getWindGust() { return windGust; }
        public List<Weather> getWeather() { return weather; }
        public double getPop() { return pop; }
    }

    public static class Daily {
        @SerializedName("dt")
        private long dt;
        @SerializedName("sunrise")
        private long sunrise;
        @SerializedName("sunset")
        private long sunset;
        @SerializedName("moonrise")
        private long moonrise;
        @SerializedName("moonset")
        private long moonset;
        @SerializedName("moon_phase")
        private double moonPhase;
        @SerializedName("summary")
        private String summary;
        @SerializedName("temp")
        private Temp temp;
        @SerializedName("feels_like")
        private FeelsLike feelsLike;
        @SerializedName("pressure")
        private int pressure;
        @SerializedName("humidity")
        private int humidity;
        @SerializedName("dew_point")
        private double dewPoint;
        @SerializedName("wind_speed")
        private double windSpeed;
        @SerializedName("wind_deg")
        private int windDeg;
        @SerializedName("wind_gust")
        private double windGust;
        @SerializedName("weather")
        private List<Weather> weather;
        @SerializedName("clouds")
        private int clouds;
        @SerializedName("pop")
        private double pop;
        @SerializedName("rain")
        private double rain;
        @SerializedName("uvi")
        private double uvi;

        public long getDt() { return dt; }
        public long getSunrise() { return sunrise; }
        public long getSunset() { return sunset; }
        public long getMoonrise() { return moonrise; }
        public long getMoonset() { return moonset; }
        public double getMoonPhase() { return moonPhase; }
        public String getSummary() { return summary; }
        public Temp getTemp() { return temp; }
        public FeelsLike getFeelsLike() { return feelsLike; }
        public int getPressure() { return pressure; }
        public int getHumidity() { return humidity; }
        public double getDewPoint() { return dewPoint; }
        public double getWindSpeed() { return windSpeed; }
        public int getWindDeg() { return windDeg; }
        public double getWindGust() { return windGust; }
        public List<Weather> getWeather() { return weather; }
        public int getClouds() { return clouds; }
        public double getPop() { return pop; }
        public double getRain() { return rain; }
        public double getUvi() { return uvi; }
    }

    public static class Temp {
        @SerializedName("day")
        private double day;
        @SerializedName("min")
        private double min;
        @SerializedName("max")
        private double max;
        @SerializedName("night")
        private double night;
        @SerializedName("eve")
        private double eve;
        @SerializedName("morn")
        private double morn;

        public double getDay() { return day; }
        public double getMin() { return min; }
        public double getMax() { return max; }
        public double getNight() { return night; }
        public double getEve() { return eve; }
        public double getMorn() { return morn; }
    }

    public static class FeelsLike {
        @SerializedName("day")
        private double day;
        @SerializedName("night")
        private double night;
        @SerializedName("eve")
        private double eve;
        @SerializedName("morn")
        private double morn;

        public double getDay() { return day; }
        public double getNight() { return night; }
        public double getEve() { return eve; }
        public double getMorn() { return morn; }
    }

    public static class Weather {
        @SerializedName("id")
        private int id;
        @SerializedName("main")
        private String main;
        @SerializedName("description")
        private String description;
        @SerializedName("icon")
        private String icon;

        public int getId() { return id; }
        public String getMain() { return main; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }

    public static class Alert {
        @SerializedName("sender_name")
        private String senderName;
        @SerializedName("event")
        private String event;
        @SerializedName("start")
        private long start;
        @SerializedName("end")
        private long end;
        @SerializedName("description")
        private String description;
        @SerializedName("tags")
        private List<String> tags;

        public String getSenderName() { return senderName; }
        public String getEvent() { return event; }
        public long getStart() { return start; }
        public long getEnd() { return end; }
        public String getDescription() { return description; }
        public List<String> getTags() { return tags; }
    }
}
