package com.example.admin.tinyweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.example.admin.tinyweather.db.City;
import com.example.admin.tinyweather.db.County;
import com.example.admin.tinyweather.db.Province;
import com.example.admin.tinyweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


//遍历省市县（解析处理JSON，简单没用GSON）+解析处理天气信息(用GSON)


public class Utility {
    //解析和处理服务器返回的省级数据
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析和处理服务器返回的市级数据
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析和处理服务器返回的县级数据
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //将返回的JSON数据解析,转换成（Weather）实体类
    public static <T> T handleHeAPIResponse(String response, Class<T> classOfT) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String strContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(strContent, classOfT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJson(Object o) {
        return new Gson().toJson(o);
    }

    public static <T> T toObject(String strContent, Class<T> classOfT) {
        return new Gson().fromJson(strContent, classOfT);
    }
}
