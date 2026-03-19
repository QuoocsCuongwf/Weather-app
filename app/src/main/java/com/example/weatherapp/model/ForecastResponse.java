package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ForecastResponse {

    @SerializedName("city")
    private City city;

    @SerializedName("list")
    private List<ForecastItem> forecastItems;

    public City getCity() {
        return city;
    }

    public List<ForecastItem> getForecastItems() {
        return forecastItems;
    }

    public static class City {
        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }
    }

    public static class ForecastItem {
        @SerializedName("dt")
        private long timestamp;

        @SerializedName("dt_txt")
        private String dateText;

        @SerializedName("main")
        private WeatherResponse.Main main;

        @SerializedName("weather")
        private List<WeatherResponse.Weather> weather;

        public long getTimestamp() {
            return timestamp;
        }

        public String getDateText() {
            return dateText;
        }

        public WeatherResponse.Main getMain() {
            return main;
        }

        public List<WeatherResponse.Weather> getWeather() {
            return weather;
        }
    }
}
