package com.example.weatherapp.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.model.CurrentWeatherModel;
import com.example.weatherapp.model.DailyForecastItem;
import com.example.weatherapp.model.GeocodingResponse;
import com.example.weatherapp.model.HourlyForecastItem;
import com.example.weatherapp.model.OneCallResponse;
import com.example.weatherapp.network.RetrofitClient;
import com.example.weatherapp.network.WeatherApiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherViewModel extends ViewModel {

    private static final String UNITS_METRIC = "metric";

    private final WeatherApiService apiService = RetrofitClient.getWeatherApiService();
    private final MutableLiveData<CurrentWeatherModel> weatherLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<HourlyForecastItem>> hourlyForecastLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<DailyForecastItem>> dailyForecastLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> rawJsonLiveData = new MutableLiveData<>("");

    public LiveData<CurrentWeatherModel> getWeatherLiveData() { return weatherLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<List<HourlyForecastItem>> getHourlyForecastLiveData() { return hourlyForecastLiveData; }
    public LiveData<List<DailyForecastItem>> getDailyForecastLiveData() { return dailyForecastLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }
    public LiveData<String> getRawJsonLiveData() { return rawJsonLiveData; }

    public void fetchWeather(@NonNull String cityName) {
        if (!checkApiKey()) return;

        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        apiService.getGeocodingByCityName(cityName, 1, BuildConfig.OPEN_WEATHER_API_KEY).enqueue(new Callback<List<GeocodingResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GeocodingResponse>> call, @NonNull Response<List<GeocodingResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    GeocodingResponse geo = response.body().get(0);
                    fetchOneCallWeather(geo.getLatitude(), geo.getLongitude(), geo.getName());
                } else {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue("Không tìm thấy thành phố. Vui lòng kiểm tra lại tên.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GeocodingResponse>> call, @NonNull Throwable throwable) {
                handleNetworkError(throwable);
            }
        });
    }

    public void fetchWeatherByCoordinates(double latitude, double longitude) {
        if (!checkApiKey()) return;

        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        // First resolve city name from coordinates using Reverse Geocoding
        apiService.getGeocodingByCoordinates(latitude, longitude, 1, BuildConfig.OPEN_WEATHER_API_KEY).enqueue(new Callback<List<GeocodingResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GeocodingResponse>> call, @NonNull Response<List<GeocodingResponse>> response) {
                String cityName = "Vị trí của bạn";
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    cityName = response.body().get(0).getName();
                }
                fetchOneCallWeather(latitude, longitude, cityName);
            }

            @Override
            public void onFailure(@NonNull Call<List<GeocodingResponse>> call, @NonNull Throwable throwable) {
                handleNetworkError(throwable);
            }
        });
    }

    private void fetchOneCallWeather(double latitude, double longitude, String cityName) {
        apiService.getOneCallWeather(latitude, longitude, BuildConfig.OPEN_WEATHER_API_KEY, "minutely,alerts", UNITS_METRIC)
                .enqueue(new Callback<OneCallResponse>() {
            @Override
            public void onResponse(@NonNull Call<OneCallResponse> call, @NonNull Response<OneCallResponse> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    processOneCallResponse(response.body(), cityName);
                } else {
                    errorLiveData.setValue("Không thể tải dữ liệu thời tiết (mã lỗi: " + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<OneCallResponse> call, @NonNull Throwable throwable) {
                handleNetworkError(throwable);
            }
        });
    }

    private void processOneCallResponse(OneCallResponse response, String cityName) {
        // Expose raw JSON
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            rawJsonLiveData.setValue(gson.toJson(response));
        } catch (Exception e) {
            rawJsonLiveData.setValue("Error formatting JSON: " + e.getMessage());
        }

        // Map Current Weather
        OneCallResponse.Current current = response.getCurrent();
        if (current != null) {
            String status = "";
            String icon = "";
            if (current.getWeather() != null && !current.getWeather().isEmpty()) {
                status = current.getWeather().get(0).getMain();
                icon = current.getWeather().get(0).getIcon();
            }
            CurrentWeatherModel currentWeather = new CurrentWeatherModel(
                    cityName, current.getTemp(), current.getHumidity(),
                    current.getWindSpeed(), status, icon,
                    current.getFeelsLike(), current.getUvi(),
                    current.getPressure(), current.getVisibility());
            weatherLiveData.setValue(currentWeather);
        }

        // Map Hourly Forecast (first 8 hours)
        List<HourlyForecastItem> hourlyItems = new ArrayList<>();
        if (response.getHourly() != null) {
            SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            for (int i = 0; i < Math.min(8, response.getHourly().size()); i++) {
                OneCallResponse.Hourly hourly = response.getHourly().get(i);
                String time = hourFormat.format(new Date(hourly.getDt() * 1000L));
                String icon = "";
                if (hourly.getWeather() != null && !hourly.getWeather().isEmpty()) {
                    icon = hourly.getWeather().get(0).getIcon();
                }
                hourlyItems.add(new HourlyForecastItem(time, hourly.getTemp(), icon, hourly.getPop()));
            }
        }
        hourlyForecastLiveData.setValue(hourlyItems);

        // Map Daily Forecast (next 7 days)
        List<DailyForecastItem> dailyItems = new ArrayList<>();
        if (response.getDaily() != null) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            for (int i = 0; i < Math.min(7, response.getDaily().size()); i++) {
                OneCallResponse.Daily daily = response.getDaily().get(i);
                String dayLabel = dayFormat.format(new Date(daily.getDt() * 1000L));
                String icon = "";
                String desc = "";
                if (daily.getWeather() != null && !daily.getWeather().isEmpty()) {
                    icon = daily.getWeather().get(0).getIcon();
                    desc = daily.getWeather().get(0).getMain();
                }
                double minTemp = daily.getTemp() != null ? daily.getTemp().getMin() : 0;
                double maxTemp = daily.getTemp() != null ? daily.getTemp().getMax() : 0;
                dailyItems.add(new DailyForecastItem(dayLabel, minTemp, maxTemp, icon, desc, daily.getPop()));
            }
        }
        dailyForecastLiveData.setValue(dailyItems);
    }

    private boolean checkApiKey() {
        if (BuildConfig.OPEN_WEATHER_API_KEY == null || BuildConfig.OPEN_WEATHER_API_KEY.trim().isEmpty()) {
            errorLiveData.setValue("API key chưa được cấu hình. Vui lòng thêm OPEN_WEATHER_API_KEY vào local.properties.");
            loadingLiveData.setValue(false);
            return false;
        }
        return true;
    }

    private void handleNetworkError(Throwable throwable) {
        loadingLiveData.setValue(false);
        if (throwable instanceof UnknownHostException) {
            errorLiveData.setValue("Không có kết nối Internet. Vui lòng thử lại.");
        } else if (throwable instanceof IOException) {
            errorLiveData.setValue("Lỗi mạng. Vui lòng thử lại sau.");
        } else {
            errorLiveData.setValue("Đã xảy ra lỗi: " + throwable.getMessage());
        }
    }
}
