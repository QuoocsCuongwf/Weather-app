package com.example.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.weatherapp.model.CurrentWeatherModel;
import com.example.weatherapp.ui.widget.ForecastWidget;
import com.example.weatherapp.ui.widget.CompassBackgroundView;
import com.example.weatherapp.viewmodel.WeatherViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Main screen of the Weather App.
 *
 * <p>Handles city search, current-location lookup, and binding all weather
 * data returned from {@link WeatherViewModel} to the UI.</p>
 */
public class MainActivity extends AppCompatActivity {

    // --- Current weather summary ---
    private EditText etCityName;
    private TextView tvCityName;
    private TextView tvTemperature;
    private TextView tvWeatherStatus;
    private TextView tvFeelsLike;
    private TextView tvMinMax;

    // --- Weather detail tiles ---
    private TextView tvPrecipitationVal;
    private TextView tvWindVal;
    private TextView tvWindDesc;
    private TextView tvAqiVal;
    private TextView tvAqiDesc;
    private TextView tvHumidityVal;
    private TextView tvDewPoint;
    private TextView tvUviVal;
    private TextView tvUviDesc;
    private TextView tvVisibilityVal;
    private TextView tvVisibilityDesc;
    private TextView tvPressureVal;

    // --- Sun / Moon tiles ---
    private TextView tvSunRise;
    private TextView tvSunSet;
    private TextView tvMoonRise;
    private TextView tvMoonSet;

    // --- Misc views ---
    private ImageView ivWeatherIcon;
    private ProgressBar progressBar;

    // --- Custom widgets ---
    private ForecastWidget hourlyForecastWidget;
    private ForecastWidget dailyForecastWidget;
    private CompassBackgroundView compassBackgroundView;

    private WeatherViewModel weatherViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initLocation();
        setupViewModel();
        setupSearchAction();

        // Use the device's current location as the default on startup.
        requestWeatherByCurrentLocation();
    }

    /**
     * Clears focus from the city-name input and hides the soft keyboard
     * whenever the user taps outside the EditText area.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View focused = getCurrentFocus();
            if (focused instanceof EditText) {
                Rect rect = new Rect();
                focused.getGlobalVisibleRect(rect);
                if (!rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    focused.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /** Binds all views and sets up button click listeners. */
    private void initViews() {
        etCityName = findViewById(R.id.etCityName);
        android.widget.ImageButton btnMyLocation = findViewById(R.id.btnMyLocation);

        tvCityName = findViewById(R.id.tvCityName);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeatherStatus = findViewById(R.id.tvWeatherStatus);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvMinMax = findViewById(R.id.tvMinMax);

        tvPrecipitationVal = findViewById(R.id.tvPrecipitationVal);
        tvWindVal = findViewById(R.id.tvWindVal);
        tvWindDesc = findViewById(R.id.tvWindDesc);
        tvAqiVal = findViewById(R.id.tvAqiVal);
        tvAqiDesc = findViewById(R.id.tvAqiDesc);
        tvHumidityVal = findViewById(R.id.tvHumidityVal);
        tvDewPoint = findViewById(R.id.tvDewPoint);
        tvUviVal = findViewById(R.id.tvUviVal);
        tvUviDesc = findViewById(R.id.tvUviDesc);
        tvVisibilityVal = findViewById(R.id.tvVisibilityVal);
        tvVisibilityDesc = findViewById(R.id.tvVisibilityDesc);
        tvPressureVal = findViewById(R.id.tvPressureVal);

        tvSunRise = findViewById(R.id.tvSunRise);
        tvSunSet = findViewById(R.id.tvSunSet);
        tvMoonRise = findViewById(R.id.tvMoonRise);
        tvMoonSet = findViewById(R.id.tvMoonSet);

        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        progressBar = findViewById(R.id.progressBar);

        hourlyForecastWidget = findViewById(R.id.hourlyForecastWidget);
        dailyForecastWidget = findViewById(R.id.dailyForecastWidget);
        compassBackgroundView = findViewById(R.id.compassBackgroundView);

        btnMyLocation.setOnClickListener(v -> requestWeatherByCurrentLocation());
    }

    /** Initialises the FusedLocationProviderClient and the permission-request launcher. */
    private void initLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                        requestLastLocation();
                    } else {
                        Toast.makeText(this, getString(R.string.error_location_permission_denied), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    /** Sets up LiveData observers on the ViewModel. */
    private void setupViewModel() {
        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        weatherViewModel.getWeatherLiveData().observe(this, this::bindWeatherData);
        weatherViewModel.getHourlyForecastLiveData().observe(this, hourlyItems ->
                hourlyForecastWidget.setForecastData(hourlyItems, false)
        );
        weatherViewModel.getDailyForecastLiveData().observe(this, dailyItems ->
                dailyForecastWidget.setForecastData(dailyItems, true)
        );
        weatherViewModel.getErrorLiveData().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        weatherViewModel.getLoadingLiveData().observe(this, isLoading ->
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE)
        );
    }

    /** Triggers a weather search when the user presses the IME Search / Enter key. */
    private void setupSearchAction() {
        etCityName.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                requestWeather();
                return true;
            }
            return false;
        });
    }

    // -------------------------------------------------------------------------
    // Weather requests
    // -------------------------------------------------------------------------

    /** Fetches weather for the city name typed in {@link #etCityName}. */
    private void requestWeather() {
        String city = etCityName.getText().toString().trim();
        if (city.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_city), Toast.LENGTH_SHORT).show();
            return;
        }
        weatherViewModel.fetchWeather(city);
    }

    /** Requests weather for the device's current GPS location. Asks for permission if needed. */
    private void requestWeatherByCurrentLocation() {
        if (hasLocationPermission()) {
            requestLastLocation();
            return;
        }
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    /** Returns {@code true} if the app has at least coarse location permission. */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /** Gets the last known device location and triggers a weather fetch by coordinates. */
    private void requestLastLocation() {
        if (!hasLocationPermission()) {
            Toast.makeText(this, getString(R.string.error_location_permission_denied), Toast.LENGTH_LONG).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        Toast.makeText(this, getString(R.string.error_location_unavailable), Toast.LENGTH_LONG).show();
                        return;
                    }
                    weatherViewModel.fetchWeatherByCoordinates(location.getLatitude(), location.getLongitude());
                })
                .addOnFailureListener(this, e ->
                        Toast.makeText(this, getString(R.string.error_location_unavailable), Toast.LENGTH_LONG).show()
                );
    }

    // -------------------------------------------------------------------------
    // Data binding
    // -------------------------------------------------------------------------

    /**
     * Populates all UI elements with data from the given {@link CurrentWeatherModel}.
     *
     * @param weather the latest weather data; does nothing if {@code null}.
     */
    private void bindWeatherData(CurrentWeatherModel weather) {
        if (weather == null) return;

        // --- Summary section ---
        tvCityName.setText(weather.getCityName());
        tvTemperature.setText(String.valueOf(Math.round(weather.getTemperature())));
        tvWeatherStatus.setText(weather.getWeatherStatus());
        loadWeatherIcon(weather.getIcon());
        tvFeelsLike.setText("Feels like " + Math.round(weather.getFeelsLike()) + "°");
        tvMinMax.setText("Night: " + Math.round(weather.getMinTemp()) + "° • Day: " + Math.round(weather.getMaxTemp()) + "°");

        // --- Detail tiles ---
        tvPrecipitationVal.setText(String.format(Locale.getDefault(), "%.1f", weather.getPrecipitation()));

        tvWindVal.setText(String.format(Locale.getDefault(), "%.1f km/h", weather.getWindSpeed() * 3.6));
        tvWindDesc.setText(String.format(Locale.getDefault(), "Gusts: %.1f km/h", weather.getWindGust() * 3.6));

        tvAqiVal.setText(String.valueOf(weather.getAqi()));
        tvAqiDesc.setText(getAqiDescription(weather.getAqi()));

        tvHumidityVal.setText(weather.getHumidity() + "%");
        tvDewPoint.setText(String.format(Locale.getDefault(), "Dew point: %d°", Math.round(weather.getDewPoint())));

        tvUviVal.setText(String.valueOf(Math.round(weather.getUvi())));
        tvUviDesc.setText(getUviDescription(weather.getUvi()));

        tvVisibilityVal.setText(String.format(Locale.getDefault(), "%.1f km", weather.getVisibility() / 1000f));
        tvVisibilityDesc.setText(weather.getVisibility() >= 10000 ? "Clear" : "Haze");

        tvPressureVal.setText(String.valueOf(weather.getPressure()));

        // --- Sun & Moon tiles ---
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvSunRise.setText(timeFormat.format(new Date(weather.getSunrise() * 1000L)));
        tvSunSet.setText(timeFormat.format(new Date(weather.getSunset() * 1000L)));
        tvMoonRise.setText(timeFormat.format(new Date(weather.getMoonrise() * 1000L)));
        tvMoonSet.setText(timeFormat.format(new Date(weather.getMoonset() * 1000L)));

        // --- Compass widget ---
        if (compassBackgroundView != null) {
            compassBackgroundView.setWindDeg(weather.getWindDeg());
        }
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Loads a weather icon from OpenWeatherMap CDN into {@link #ivWeatherIcon}.
     *
     * @param iconCode the icon code returned by the API (e.g. {@code "01d"}).
     */
    private void loadWeatherIcon(String iconCode) {
        if (iconCode == null || iconCode.trim().isEmpty()) {
            ivWeatherIcon.setImageDrawable(null);
            return;
        }
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        Glide.with(this)
                .load(iconUrl)
                .into(ivWeatherIcon);
    }

    /**
     * Maps an AQI index (1–5, as defined by OpenWeatherMap) to a human-readable label.
     *
     * @param aqi Air Quality Index value from the API.
     * @return a descriptive string such as "Good" or "Very Poor".
     */
    private static String getAqiDescription(int aqi) {
        switch (aqi) {
            case 2: return "Fair";
            case 3: return "Moderate";
            case 4: return "Poor";
            case 5: return "Very Poor";
            default: return "Good";
        }
    }

    /**
     * Maps a UV index value to a WHO risk-level label.
     *
     * @param uvi UV index from the API.
     * @return a descriptive string such as "Low" or "Extreme".
     */
    private static String getUviDescription(double uvi) {
        if (uvi >= 11) return "Extreme";
        if (uvi >= 8)  return "Very High";
        if (uvi >= 6)  return "High";
        if (uvi >= 3)  return "Moderate";
        return "Low";
    }

    /**
     * Maps a moon-phase value (0–1, as defined by OpenWeatherMap) to a phase name.
     *
     * @param moonPhase fractional illumination value where 0 and 1 represent New Moon.
     * @return the name of the current moon phase.
     */
    private static String getMoonPhaseDescription(double moonPhase) {
        if (moonPhase > 0 && moonPhase < 0.25)  return "Waxing crescent";
        if (moonPhase == 0.25)                   return "First quarter moon";
        if (moonPhase > 0.25 && moonPhase < 0.5) return "Waxing gibbous";
        if (moonPhase == 0.5)                    return "Full moon";
        if (moonPhase > 0.5 && moonPhase < 0.75) return "Waning gibbous";
        if (moonPhase == 0.75)                   return "Last quarter moon";
        if (moonPhase > 0.75 && moonPhase < 1.0) return "Waning crescent";
        return "New moon";
    }
}
