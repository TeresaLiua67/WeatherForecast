package com.example.admin.tinyweather.gson;

import com.google.gson.annotations.SerializedName;



public class Basic {
    @SerializedName("location")
    public String cityName;

    @SerializedName("cid")
    public String cityId;
}
