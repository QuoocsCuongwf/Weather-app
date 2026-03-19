package com.example.weatherapp.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weatherapp.BuildConfig;
import com.example.weatherapp.model.DailyForecastItem;
import com.example.weatherapp.model.ForecastResponse;
import com.example.weatherapp.model.HourlyForecastItem;
import com.example.weatherapp.model.WeatherResponse;
import com.example.weatherapp.network.RetrofitClient;
import com.example.weatherapp.network.WeatherApiService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherViewModel extends ViewModel {

    private static final String UNITS_METRIC = "metric";

    private final WeatherApiService apiService = RetrofitClient.getWeatherApiService();
    private final MutableLiveData<WeatherResponse> weatherLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<HourlyForecastItem>> hourlyForecastLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<DailyForecastItem>> dailyForecastLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    public LiveData<WeatherResponse> getWeatherLiveData() {
        return weatherLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<List<HourlyForecastItem>> getHourlyForecastLiveData() {
        return hourlyForecastLiveData;
    }

    public LiveData<List<DailyForecastItem>> getDailyForecastLiveData() {
        return dailyForecastLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public void fetchWeather(@NonNull String cityName) {
        if (BuildConfig.OPEN_WEATHER_API_KEY == null || BuildConfig.OPEN_WEATHER_API_KEY.trim().isEmpty()) {
            errorLiveData.setValue("API key chưa được cấu hình. Vui lòng thêm OPEN_WEATHER_API_KEY vào local.properties.");
            loadingLiveData.setValue(false);
            return;
        }

        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        apiService.getCurrentWeather(cityName, BuildConfig.OPEN_WEATHER_API_KEY, UNITS_METRIC)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                        loadingLiveData.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            WeatherResponse weatherResponse = response.body();
                            weatherLiveData.setValue(weatherResponse);
                            WeatherResponse.Coordinates coordinates = weatherResponse.getCoordinates();
                            if (coordinates != null) {
                                requestForecastByCoordinates(
                                        coordinates.getLatitude(),
                                        coordinates.getLongitude()
                                );
                            } else {
                                hourlyForecastLiveData.setValue(new ArrayList<>());
                                dailyForecastLiveData.setValue(new ArrayList<>());
                            }
                            return;
                        }

                        if (response.code() == 404) {
                            errorLiveData.setValue("Không tìm thấy thành phố. Vui lòng kiểm tra lại tên.");
                        } else {
                            errorLiveData.setValue("Không thể tải dữ liệu thời tiết (mã lỗi: " + response.code() + ").");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable throwable) {
                        loadingLiveData.setValue(false);
                        if (throwable instanceof UnknownHostException) {
                            errorLiveData.setValue("Không có kết nối Internet. Vui lòng thử lại.");
                        } else if (throwable instanceof IOException) {
                            errorLiveData.setValue("Lỗi mạng. Vui lòng thử lại sau.");
                        } else {
                            errorLiveData.setValue("Đã xảy ra lỗi: " + throwable.getMessage());
                        }
                    }
                });
    }

    public void fetchWeatherByCoordinates(double latitude, double longitude) {
        if (BuildConfig.OPEN_WEATHER_API_KEY == null || BuildConfig.OPEN_WEATHER_API_KEY.trim().isEmpty()) {
            errorLiveData.setValue("API key chưa được cấu hình. Vui lòng thêm OPEN_WEATHER_API_KEY vào local.properties.");
            loadingLiveData.setValue(false);
            return;
        }

        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        apiService.getCurrentWeatherByCoordinates(latitude, longitude, BuildConfig.OPEN_WEATHER_API_KEY, UNITS_METRIC)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                        loadingLiveData.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            weatherLiveData.setValue(response.body());
                            requestForecastByCoordinates(latitude, longitude);
                            return;
                        }
                        errorLiveData.setValue("Không thể tải dữ liệu vị trí hiện tại (mã lỗi: " + response.code() + ").");
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable throwable) {
                        loadingLiveData.setValue(false);
                        if (throwable instanceof UnknownHostException) {
                            errorLiveData.setValue("Không có kết nối Internet. Vui lòng thử lại.");
                        } else if (throwable instanceof IOException) {
                            errorLiveData.setValue("Lỗi mạng. Vui lòng thử lại sau.");
                        } else {
                            errorLiveData.setValue("Đã xảy ra lỗi: " + throwable.getMessage());
                        }
                    }
                });
    }

    private void requestForecastByCoordinates(double latitude, double longitude) {
        apiService.getForecastByCoordinates(latitude, longitude, BuildConfig.OPEN_WEATHER_API_KEY, UNITS_METRIC)
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ForecastResponse> call, @NonNull Response<ForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ForecastResponse.ForecastItem> forecastItems = response.body().getForecastItems();
                            if (forecastItems == null) {
                                hourlyForecastLiveData.setValue(new ArrayList<>());
                                dailyForecastLiveData.setValue(new ArrayList<>());
                                return;
                            }
                            hourlyForecastLiveData.setValue(mapHourlyForecast(forecastItems));
                            dailyForecastLiveData.setValue(mapDailyForecast(forecastItems));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable throwable) {
                        // Keep current weather visible if only forecast fails.
                    }
                });
    }

    private List<HourlyForecastItem> mapHourlyForecast(List<ForecastResponse.ForecastItem> forecastItems) {
        List<HourlyForecastItem> hourlyItems = new ArrayList<>();
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        for (ForecastResponse.ForecastItem item : forecastItems) {
            if (hourlyItems.size() >= 8) {
                break;
            }
            WeatherResponse.Main main = item.getMain();
            if (main == null) {
                continue;
            }
            String icon = "";
            if (item.getWeather() != null && !item.getWeather().isEmpty()) {
                icon = item.getWeather().get(0).getIcon();
            }
            String time = hourFormat.format(new Date(item.getTimestamp() * 1000L));
            hourlyItems.add(new HourlyForecastItem(time, main.getTemperature(), icon));
        }
        return hourlyItems;
    }

    private List<DailyForecastItem> mapDailyForecast(List<ForecastResponse.ForecastItem> forecastItems) {
        Map<String, List<ForecastResponse.ForecastItem>> groupedByDay = new LinkedHashMap<>();
        for (ForecastResponse.ForecastItem item : forecastItems) {
            String dateText = item.getDateText();
            if (dateText == null || dateText.length() < 10) {
                continue;
            }
            String key = dateText.substring(0, 10);
            if (!groupedByDay.containsKey(key)) {
                groupedByDay.put(key, new ArrayList<>());
            }
            groupedByDay.get(key).add(item);
        }

        List<DailyForecastItem> dailyItems = new ArrayList<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        for (Map.Entry<String, List<ForecastResponse.ForecastItem>> entry : groupedByDay.entrySet()) {
            if (dailyItems.size() >= 7) {
                break;
            }
            List<ForecastResponse.ForecastItem> dayItems = entry.getValue();
            double minTemperature = Double.MAX_VALUE;
            double maxTemperature = -Double.MAX_VALUE;
            String icon = "";
            String description = "";
            for (ForecastResponse.ForecastItem dayItem : dayItems) {
                WeatherResponse.Main main = dayItem.getMain();
                if (main == null) {
                    continue;
                }
                minTemperature = Math.min(minTemperature, main.getMinTemperature());
                maxTemperature = Math.max(maxTemperature, main.getMaxTemperature());
                if (icon.isEmpty() && dayItem.getWeather() != null && !dayItem.getWeather().isEmpty()) {
                    icon = dayItem.getWeather().get(0).getIcon();
                    description = dayItem.getWeather().get(0).getMain();
                }
            }
            if (minTemperature == Double.MAX_VALUE || maxTemperature == -Double.MAX_VALUE) {
                continue;
            }
            long timestampSeconds = dayItems.get(0).getTimestamp();
            String dayLabel = dayFormat.format(new Date(timestampSeconds * 1000L));
            dailyItems.add(new DailyForecastItem(dayLabel, minTemperature, maxTemperature, icon, description));
        }
        return dailyItems;
    }
}
