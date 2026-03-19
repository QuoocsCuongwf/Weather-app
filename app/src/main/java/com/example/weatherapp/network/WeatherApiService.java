package com.example.weatherapp.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("geo/1.0/direct")
    Call<List<com.example.weatherapp.model.GeocodingResponse>> getGeocodingByCityName(
            @Query("q") String cityName,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );

    @GET("geo/1.0/reverse")
    Call<List<com.example.weatherapp.model.GeocodingResponse>> getGeocodingByCoordinates(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );

    @GET("data/3.0/onecall")
    Call<com.example.weatherapp.model.OneCallResponse> getOneCallWeather(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey,
            @Query("exclude") String exclude,
            @Query("units") String units
//            @Query("lang") String units
    );
}
