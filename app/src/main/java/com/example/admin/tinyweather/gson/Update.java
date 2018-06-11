package com.example.admin.tinyweather.gson;

import com.google.gson.annotations.SerializedName;



public class Update {
    @SerializedName("loc")
    public String localUpdateTime;

    @SerializedName("utc")
    public String utcUpdateTime;
}
