package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AirPollutionResponse {
    @SerializedName("list")
    private List<PollutionList> list;

    public List<PollutionList> getList() { return list; }

    public static class PollutionList {
        @SerializedName("main")
        private Main main;

        public Main getMain() { return main; }
    }

    public static class Main {
        @SerializedName("aqi")
        private int aqi;

        public int getAqi() { return aqi; }
    }
}
