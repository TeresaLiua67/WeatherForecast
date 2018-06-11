package com.example.admin.tinyweather.data;



import com.example.admin.tinyweather.gson.AQI;
import com.example.admin.tinyweather.gson.Weather;

/**
 * 数据源接口
 */
public interface DataSource {
    // 天气加载完成回调
    interface LoadWeatherDataCallback {
        void onWeatherDataLoaded(Weather weather);
        void onWeatherDataNotAvailable();
    }

    // 空气质量信息加载完成回调
    interface LoadAQIDataCallback {
        void onAQIDataLoaded(AQI aqi);
        void onAQIDataNotAvailable();
    }

    void loadWeatherData(String city, LoadWeatherDataCallback callback);

    void loadAQIData(String city, LoadAQIDataCallback callback);

    void saveWeatherData(String city, String weatherString);

    void saveAQIData(String city, String aqiString);
}
