package com.example.admin.tinyweather.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.admin.tinyweather.data.DataSource;
import com.example.admin.tinyweather.gson.AQI;
import com.example.admin.tinyweather.gson.Weather;
import com.example.admin.tinyweather.util.Utility;

//本地数据源，相当于缓存

public class LocalDataSource implements DataSource {
    private Context mCtx;

    public LocalDataSource (Context context) {
        mCtx = context;
    }

    @Override
    public void loadWeatherData(String city, LoadWeatherDataCallback callback) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.toObject(weatherString, Weather.class);
            if (weather != null && TextUtils.isEmpty(city)) { //如果请求的城市为空，说明是刚启动加载，就直接显示缓存数据好了
                callback.onWeatherDataLoaded(weather);
            } else if (weather != null && city.equals(weather.basic.cityName)) {    //如果缓存的城市数据数据是所需的城市才采用之

                callback.onWeatherDataLoaded(weather);
            } else {
                callback.onWeatherDataNotAvailable();
            }
        } else {
            callback.onWeatherDataNotAvailable();
        }
    }

    @Override
    public void loadAQIData(String city, LoadAQIDataCallback callback) {
        // current do nothing
    }

    @Override
    public void saveWeatherData(String city, String weatherString) {//保存数据
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(mCtx).edit();
        editor.putString("weather", weatherString);
        editor.apply();
    }

    @Override
    public void saveAQIData(String city, String aqiString) {
        // current do nothing
    }
}
