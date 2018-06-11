package com.example.admin.tinyweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;



public class HttpUtil {
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {   //发起一个http请求，只需要调用sendOkHttpRequest方法
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();   //传入请求地址
        client.newCall(request).enqueue(callback);  //注册一个回调来处理服务器响应
    }
}
