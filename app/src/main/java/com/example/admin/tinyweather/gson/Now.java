package com.example.admin.tinyweather.gson;

import com.google.gson.annotations.SerializedName;



public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond_txt")
    public String weatherInfo;
}
