package com.example.admin.tinyweather.data.remote;

import android.widget.Toast;

import com.example.admin.tinyweather.data.DataSource;
import com.example.admin.tinyweather.gson.AQI;
import com.example.admin.tinyweather.gson.Weather;
import com.example.admin.tinyweather.util.HttpUtil;
import com.example.admin.tinyweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//远程数据源，相当于从上面请求来的

public class RemoteDataSource implements DataSource {
    private static final String WEATHER_URL = "https://free-api.heweather.com/s6/weather?" +
            "key=34fcb36bcc8a42d2b0fe9b549cce8f8c&location=";
    private static final String AQI_URL = "https://free-api.heweather.com/s6/air/now?" +
            "key=34fcb36bcc8a42d2b0fe9b549cce8f8c&location=";

    @Override
    public void loadWeatherData(String city, LoadWeatherDataCallback callback) {
        requestWeather(city, callback);
    }

    @Override
    public void loadAQIData(String city, LoadAQIDataCallback callback) {
        requestAQI(city, callback);
    }

    @Override
    public void saveWeatherData(String city, String weatherString) {
        // current do nothing here
    }

    @Override
    public void saveAQIData(String city, String aqiString) {
        // current do nothing here
    }

    public void requestWeather(final String countyName, final LoadWeatherDataCallback callback) {
        String weatherUrl =  WEATHER_URL + countyName;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {//发送http请求天气信息
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onWeatherDataNotAvailable();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleHeAPIResponse(responseText, Weather.class);//解析天气信息
                callback.onWeatherDataLoaded(weather);//解析好后加载出来
            }
        });
        //loadBingPicPathFromGuolinAPI();
    }

    public void requestAQI(final String countyName, final LoadAQIDataCallback callback) {
        String aqiUrl = AQI_URL + countyName;
        HttpUtil.sendOkHttpRequest(aqiUrl, new Callback() {//发送http请求空气质量信息
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onAQIDataNotAvailable();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final AQI aqi = Utility.handleHeAPIResponse(responseText, AQI.class);//解析空气质量
                callback.onAQIDataLoaded(aqi);//解析好后加载出来
            }
        });
    }
}
