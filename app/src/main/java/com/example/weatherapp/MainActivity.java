package com.example.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.ui.DailyForecastAdapter;
import com.example.weatherapp.ui.HourlyForecastAdapter;
import com.example.weatherapp.model.CurrentWeatherModel;
import com.example.weatherapp.viewmodel.WeatherViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {

    private EditText etCityName;
    private TextView tvCityName;
    private TextView tvTemperature;
    private TextView tvWeatherStatus;
    private TextView tvHumidity;
    private TextView tvWindSpeed;
    private TextView tvFeelsLike;
    private TextView tvUvi;
    private TextView tvPressure;
    private TextView tvVisibility;
    private ImageView ivWeatherIcon;
    private ProgressBar progressBar;
    private HourlyForecastAdapter hourlyForecastAdapter;
    private DailyForecastAdapter dailyForecastAdapter;
    private TextView tvRawJson;

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

        // Load default city when app opens first time.
        etCityName.setText("Ha Noi");
        requestWeather();
    }

    private void initViews() {
        etCityName = findViewById(R.id.etCityName);
        ImageButton btnSearch = findViewById(R.id.btnSearch);
        ImageButton btnMyLocation = findViewById(R.id.btnMyLocation);
        tvCityName = findViewById(R.id.tvCityName);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeatherStatus = findViewById(R.id.tvWeatherStatus);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvUvi = findViewById(R.id.tvUvi);
        tvPressure = findViewById(R.id.tvPressure);
        tvVisibility = findViewById(R.id.tvVisibility);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        progressBar = findViewById(R.id.progressBar);
        tvRawJson = findViewById(R.id.tvRawJson);
        RecyclerView rvHourlyForecast = findViewById(R.id.rvHourlyForecast);
        RecyclerView rvDailyForecast = findViewById(R.id.rvDailyForecast);

        hourlyForecastAdapter = new HourlyForecastAdapter();
        dailyForecastAdapter = new DailyForecastAdapter();

        rvHourlyForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHourlyForecast.setAdapter(hourlyForecastAdapter);

        rvDailyForecast.setLayoutManager(new LinearLayoutManager(this));
        rvDailyForecast.setNestedScrollingEnabled(false);
        rvDailyForecast.setAdapter(dailyForecastAdapter);

        btnSearch.setOnClickListener(v -> requestWeather());
        btnMyLocation.setOnClickListener(v -> requestWeatherByCurrentLocation());
    }

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

    private void setupViewModel() {
        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        weatherViewModel.getWeatherLiveData().observe(this, this::bindWeatherData);
        weatherViewModel.getHourlyForecastLiveData().observe(this, hourlyItems ->
                hourlyForecastAdapter.submitItems(hourlyItems)
        );
        weatherViewModel.getDailyForecastLiveData().observe(this, dailyItems ->
                dailyForecastAdapter.submitItems(dailyItems)
        );
        weatherViewModel.getErrorLiveData().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        weatherViewModel.getLoadingLiveData().observe(this, isLoading ->
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE)
        );
        weatherViewModel.getRawJsonLiveData().observe(this, rawJson ->
                tvRawJson.setText(rawJson)
        );
    }

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

    private void requestWeather() {
        String city = etCityName.getText().toString().trim();
        if (city.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_city), Toast.LENGTH_SHORT).show();
            return;
        }
        weatherViewModel.fetchWeather(city);
    }

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

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

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

    private void bindWeatherData(CurrentWeatherModel weatherResponse) {
        if (weatherResponse == null) {
            return;
        }

        tvCityName.setText(weatherResponse.getCityName());
        tvTemperature.setText(getString(R.string.temperature_format, weatherResponse.getTemperature()));
        tvHumidity.setText(getString(R.string.humidity_format, weatherResponse.getHumidity()));
        tvWindSpeed.setText(getString(R.string.wind_speed_format, weatherResponse.getWindSpeed()));
        tvFeelsLike.setText(getString(R.string.feels_like_format, weatherResponse.getFeelsLike()));
        tvUvi.setText(getString(R.string.uvi_format, weatherResponse.getUvi()));
        tvPressure.setText(getString(R.string.pressure_format, weatherResponse.getPressure()));
        tvVisibility.setText(getString(R.string.visibility_format, weatherResponse.getVisibility()));
        tvWeatherStatus.setText(weatherResponse.getWeatherStatus());
        loadWeatherIcon(weatherResponse.getIcon());
    }

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
}
