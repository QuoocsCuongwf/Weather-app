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

/**
 * ViewModel that orchestrates all weather data fetching for the main screen.
 *
 * <p>Fetch flow:
 * <ol>
 *   <li>Geocoding (city name or reverse) → lat/lon + display name</li>
 *   <li>One Call API 3.0 → current / hourly / daily weather</li>
 *   <li>Air Pollution API → AQI (runs in parallel with step 2's response callback)</li>
 * </ol>
 * Exposes results via {@link LiveData} objects observed by the Activity.</p>
 */
public class WeatherViewModel extends ViewModel {

    private static final String UNITS_METRIC = "metric";

    private final WeatherApiService apiService = RetrofitClient.getWeatherApiService();

    private final MutableLiveData<CurrentWeatherModel> weatherLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<HourlyForecastItem>> hourlyForecastLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<DailyForecastItem>> dailyForecastLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    public LiveData<CurrentWeatherModel> getWeatherLiveData() { return weatherLiveData; }
    public LiveData<List<HourlyForecastItem>> getHourlyForecastLiveData() { return hourlyForecastLiveData; }
    public LiveData<List<DailyForecastItem>> getDailyForecastLiveData() { return dailyForecastLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Fetches weather data for the given city name.
     * Resolves coordinates via the Geocoding API first.
     *
     * @param cityName name of the city to look up (e.g. "Ha Noi").
     */
    public void fetchWeather(@NonNull String cityName) {
        if (!checkApiKey()) return;

        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        apiService.getGeocodingByCityName(cityName, 1, BuildConfig.OPEN_WEATHER_API_KEY)
                .enqueue(new Callback<List<GeocodingResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<GeocodingResponse>> call,
                                           @NonNull Response<List<GeocodingResponse>> response) {
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

    /**
     * Fetches weather data for a specific GPS coordinate pair.
     * Resolves a display name via Reverse Geocoding first.
     *
     * @param latitude  WGS-84 latitude.
     * @param longitude WGS-84 longitude.
     */
    public void fetchWeatherByCoordinates(double latitude, double longitude) {
        if (!checkApiKey()) return;

        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        // Resolve a human-readable city name from the coordinates.
        apiService.getGeocodingByCoordinates(latitude, longitude, 1, BuildConfig.OPEN_WEATHER_API_KEY)
                .enqueue(new Callback<List<GeocodingResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<GeocodingResponse>> call,
                                           @NonNull Response<List<GeocodingResponse>> response) {
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

    // -------------------------------------------------------------------------
    // Private fetch chain
    // -------------------------------------------------------------------------

    /**
     * Step 2 of the fetch chain: calls the One Call API with the resolved coordinates.
     */
    private void fetchOneCallWeather(double latitude, double longitude, String cityName) {
        apiService.getOneCallWeather(latitude, longitude, BuildConfig.OPEN_WEATHER_API_KEY, "minutely,alerts", UNITS_METRIC)
                .enqueue(new Callback<OneCallResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<OneCallResponse> call,
                                           @NonNull Response<OneCallResponse> response) {
                        loadingLiveData.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            fetchAirPollutionAndProcess(latitude, longitude, cityName, response.body());
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

    /**
     * Step 3 of the fetch chain: fetches AQI, then processes all weather data together.
     * Falls back to AQI = 1 (Good) if the air-pollution request fails.
     */
    private void fetchAirPollutionAndProcess(double latitude, double longitude,
                                              String cityName, OneCallResponse oneCallResponse) {
        apiService.getAirPollution(latitude, longitude, BuildConfig.OPEN_WEATHER_API_KEY)
                .enqueue(new Callback<com.example.weatherapp.model.AirPollutionResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<com.example.weatherapp.model.AirPollutionResponse> call,
                                           @NonNull Response<com.example.weatherapp.model.AirPollutionResponse> response) {
                        int aqi = 1; // Default to "Good"
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getList() != null
                                && !response.body().getList().isEmpty()) {
                            aqi = response.body().getList().get(0).getMain().getAqi();
                        }
                        processOneCallResponse(oneCallResponse, cityName, aqi);
                    }

                    @Override
                    public void onFailure(@NonNull Call<com.example.weatherapp.model.AirPollutionResponse> call,
                                          @NonNull Throwable throwable) {
                        // Continue with degraded data rather than showing an error.
                        processOneCallResponse(oneCallResponse, cityName, 1);
                    }
                });
    }

    // -------------------------------------------------------------------------
    // Data processing
    // -------------------------------------------------------------------------

    /**
     * Maps the raw {@link OneCallResponse} (plus AQI) to the LiveData models consumed by the UI.
     */
    private void processOneCallResponse(OneCallResponse response, String cityName, int aqi) {
        mapCurrentWeather(response, cityName, aqi);
        mapHourlyForecast(response);
        mapDailyForecast(response, aqi);
    }

    /** Maps the {@code current} block of the One Call response to {@link CurrentWeatherModel}. */
    private void mapCurrentWeather(OneCallResponse response, String cityName, int aqi) {
        OneCallResponse.Current current = response.getCurrent();
        if (current == null) return;

        // Extract weather condition fields.
        String status = "";
        String icon = "";
        if (current.getWeather() != null && !current.getWeather().isEmpty()) {
            status = current.getWeather().get(0).getMain();
            icon = current.getWeather().get(0).getIcon();
        }

        // Use minutely precipitation if available, otherwise fall back to today's daily rain total.
        double precipitation = 0;
        if (response.getMinutely() != null && !response.getMinutely().isEmpty()) {
            precipitation = response.getMinutely().get(0).getPrecipitation();
        } else if (response.getDaily() != null && !response.getDaily().isEmpty()) {
            precipitation = response.getDaily().get(0).getRain();
        }

        // Extract today's sun/moon data from the first daily entry, or fall back to current block.
        long sunrise = 0, sunset = 0, moonrise = 0, moonset = 0;
        double moonPhase = 0;
        double minTemp = 0, maxTemp = 0;
        if (response.getDaily() != null && !response.getDaily().isEmpty()) {
            OneCallResponse.Daily today = response.getDaily().get(0);
            sunrise = today.getSunrise();
            sunset = today.getSunset();
            moonrise = today.getMoonrise();
            moonset = today.getMoonset();
            moonPhase = today.getMoonPhase();
            if (today.getTemp() != null) {
                minTemp = today.getTemp().getMin();
                maxTemp = today.getTemp().getMax();
            }
        } else {
            sunrise = current.getSunrise();
            sunset = current.getSunset();
        }

        weatherLiveData.setValue(new CurrentWeatherModel(
                cityName, current.getTemp(), current.getHumidity(),
                current.getWindSpeed(), status, icon,
                current.getFeelsLike(), current.getUvi(),
                current.getPressure(), current.getVisibility(),
                precipitation, current.getWindGust(), current.getWindDeg(), aqi, current.getDewPoint(),
                sunrise, sunset, moonrise, moonset, moonPhase, minTemp, maxTemp));
    }

    /** Maps the first 24 hourly entries to {@link HourlyForecastItem} objects. */
    private void mapHourlyForecast(OneCallResponse response) {
        List<HourlyForecastItem> hourlyItems = new ArrayList<>();
        if (response.getHourly() == null) {
            hourlyForecastLiveData.setValue(hourlyItems);
            return;
        }

        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        int count = Math.min(24, response.getHourly().size());
        for (int i = 0; i < count; i++) {
            OneCallResponse.Hourly hourly = response.getHourly().get(i);
            String time = hourFormat.format(new Date(hourly.getDt() * 1000L));
            String icon = hourly.getWeather() != null && !hourly.getWeather().isEmpty()
                    ? hourly.getWeather().get(0).getIcon() : "";

            hourlyItems.add(new HourlyForecastItem(
                    hourly.getDt(), time, hourly.getTemp(),
                    icon, buildNightIcon(icon), hourly.getPop(),
                    hourly.getHumidity(), hourly.getUvi(),
                    hourly.getWindSpeed(), hourly.getWindGust(), hourly.getWindDeg()
            ));
        }
        hourlyForecastLiveData.setValue(hourlyItems);
    }

    /** Maps up to 8 daily entries to {@link DailyForecastItem} objects. */
    private void mapDailyForecast(OneCallResponse response, int aqi) {
        List<DailyForecastItem> dailyItems = new ArrayList<>();
        if (response.getDaily() == null) {
            dailyForecastLiveData.setValue(dailyItems);
            return;
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        int count = Math.min(8, response.getDaily().size());
        for (int i = 0; i < count; i++) {
            OneCallResponse.Daily daily = response.getDaily().get(i);
            String dayLabel = dayFormat.format(new Date(daily.getDt() * 1000L));
            String icon = "";
            String description = "";
            if (daily.getWeather() != null && !daily.getWeather().isEmpty()) {
                icon = daily.getWeather().get(0).getIcon();
                description = daily.getWeather().get(0).getMain();
            }

            double minTemp = daily.getTemp() != null ? daily.getTemp().getMin() : 0;
            double maxTemp = daily.getTemp() != null ? daily.getTemp().getMax() : 0;

            dailyItems.add(new DailyForecastItem(
                    daily.getDt(), dayLabel, minTemp, maxTemp,
                    icon, buildNightIcon(icon), description, daily.getPop(),
                    daily.getUvi(), daily.getWindSpeed(), daily.getWindDeg(),
                    daily.getWindGust(), aqi, daily.getHumidity()
            ));
        }
        dailyForecastLiveData.setValue(dailyItems);
    }

    // -------------------------------------------------------------------------
    // Utility helpers
    // -------------------------------------------------------------------------

    /**
     * Derives the night-variant icon code from a day icon code by replacing the
     * trailing {@code "d"} suffix with {@code "n"}.
     *
     * <p>Example: {@code "01d"} → {@code "01n"}</p>
     *
     * @param dayIcon the day icon code (may be empty or null).
     * @return the night icon code, or an empty string if {@code dayIcon} is empty.
     */
    private static String buildNightIcon(String dayIcon) {
        if (dayIcon == null || dayIcon.isEmpty()) return "";
        return dayIcon.substring(0, dayIcon.length() - 1) + "n";
    }

    /**
     * Validates that the API key is configured before making any network call.
     *
     * @return {@code true} if the key is present; {@code false} and posts an error otherwise.
     */
    private boolean checkApiKey() {
        if (BuildConfig.OPEN_WEATHER_API_KEY == null || BuildConfig.OPEN_WEATHER_API_KEY.trim().isEmpty()) {
            errorLiveData.setValue("API key chưa được cấu hình. Vui lòng thêm OPEN_WEATHER_API_KEY vào local.properties.");
            loadingLiveData.setValue(false);
            return false;
        }
        return true;
    }

    /**
     * Translates a network throwable into a user-friendly error message.
     *
     * @param throwable the exception raised by Retrofit.
     */
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
