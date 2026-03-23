package com.example.weatherapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provides a lazily-initialised Retrofit singleton configured for
 * the OpenWeatherMap base URL.
 *
 * <p>Use {@link #getWeatherApiService()} to obtain the API interface.</p>
 */
public final class RetrofitClient {

    private static final String BASE_URL = "https://api.openweathermap.org/";
    private static Retrofit retrofit;

    private RetrofitClient() {
    }

    /** Returns the singleton {@link WeatherApiService}, creating it on first call. */
    public static WeatherApiService getWeatherApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(WeatherApiService.class);
    }
}
