package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;

public class GeocodingResponse {

    @SerializedName("name")
    private String name;

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lon")
    private double longitude;

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
