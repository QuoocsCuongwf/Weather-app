package com.example.weatherapp.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit service interface that declares all OpenWeatherMap API endpoints
 * used by the application.
 */
public interface WeatherApiService {

    /** Resolves a city name to geographic coordinates (forward geocoding). */
    @GET("geo/1.0/direct")
    Call<List<com.example.weatherapp.model.GeocodingResponse>> getGeocodingByCityName(
            @Query("q") String cityName,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );

    /** Resolves geographic coordinates to a city name (reverse geocoding). */
    @GET("geo/1.0/reverse")
    Call<List<com.example.weatherapp.model.GeocodingResponse>> getGeocodingByCoordinates(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );

    /** Fetches current, hourly, and daily weather via the One Call API 3.0. */
    @GET("data/3.0/onecall")
    Call<com.example.weatherapp.model.OneCallResponse> getOneCallWeather(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey,
            @Query("exclude") String exclude,
            @Query("units") String units
    );

    /** Fetches the current Air Quality Index for the given coordinates. */
    @GET("data/2.5/air_pollution")
    Call<com.example.weatherapp.model.AirPollutionResponse> getAirPollution(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey
    );
}
