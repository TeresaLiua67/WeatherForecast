package com.example.admin.tinyweather.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.admin.tinyweather.WeatherActivity;
import com.example.admin.tinyweather.gson.AQI;
import com.example.admin.tinyweather.gson.Weather;
import com.example.admin.tinyweather.util.HttpUtil;
import com.example.admin.tinyweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;



public class UpdateJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        updateWeather();
        updateAQI();
        updateBingPic();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // 如果任务被取消了，可让系统重新安排，返回true可达到此目的
        return true;
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weathrString = prefs.getString("weather", null);
        if (weathrString != null) {
            // 如有缓存，解析缓存，获取缓存的是哪个城市的天气，获取城市名，去更新这城市的天气
            Weather weather = Utility.handleHeAPIResponse(weathrString, Weather.class);
            String cityName = weather.basic.cityName;

            String queryUrl = "https://free-api.heweather.com/s6/weather?" +
                    "key=34fcb36bcc8a42d2b0fe9b549cce8f8c&location=" + cityName;
            HttpUtil.sendOkHttpRequest(queryUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleHeAPIResponse(responseText, Weather.class);
                    // 如果查询到的数据能成功解析，那就把返回的内容存到缓存中，方便下次直接显示较新内容
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(UpdateJobService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });

        }
    }

    private void updateAQI() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weathrString = prefs.getString("weather", null);
        if (weathrString != null) {
            // 如有缓存，解析缓存，获取缓存的是哪个城市的天气，获取城市名，去更新这城市的空气质量
            Weather weather = Utility.handleHeAPIResponse(weathrString, Weather.class);
            String cityName = weather.basic.cityName;

            String aqiUrl = "https://free-api.heweather.com/s6/air/now?" +
                    "key=34fcb36bcc8a42d2b0fe9b549cce8f8c&location=" + cityName;
            HttpUtil.sendOkHttpRequest(aqiUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText = response.body().string();
                    final AQI aqi = Utility.handleHeAPIResponse(responseText, AQI.class);
                    // 如果查询到的数据能成功解析，那就把返回的内容存到缓存中，方便下次直接显示较新内容
                    if (aqi != null && "ok".equals(aqi.status)) {
                        SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(UpdateJobService.this).edit();
                        editor.putString("aqi", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    private void updateBingPic() {
        String requestBingPicUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPicUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            //获取到必应背景图链接，然后缓存到SharedPreference中
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(UpdateJobService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }
}
