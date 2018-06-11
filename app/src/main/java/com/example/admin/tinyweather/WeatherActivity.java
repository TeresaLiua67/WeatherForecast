package com.example.admin.tinyweather;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.admin.tinyweather.data.DataRepository;
import com.example.admin.tinyweather.data.DataSource;
//import com.example.admin.tinyweather.data.remote.DataSource;
import com.example.admin.tinyweather.data.local.LocalDataSource;
import com.example.admin.tinyweather.data.remote.RemoteDataSource;
import com.example.admin.tinyweather.gson.AQI;
import com.example.admin.tinyweather.gson.Forecast;
import com.example.admin.tinyweather.gson.Suggestion;
import com.example.admin.tinyweather.gson.Weather;
import com.example.admin.tinyweather.service.UpdateJobService;
import com.example.admin.tinyweather.util.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherScrollView;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private LinearLayout suggestionLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private ImageView bingImg;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    private String mCurrentCityName;

    public DrawerLayout mDrawerLayout;
    private Button navButton;
    private DataRepository mDataRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //让背景图和状态栏融合到一起
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);   //活动布局会显示在状态栏上面
            getWindow().setStatusBarColor(Color.TRANSPARENT);   //透明色
        }
        setContentView(R.layout.activity_weather);

        mDataRepository = DataRepository.getInstance
                (new RemoteDataSource(), new LocalDataSource(this));
        mCurrentCityName = getIntent().getStringExtra("county_name");
        //初始化各种控件
        weatherScrollView = findViewById(R.id.sv_weather);
        titleCity = findViewById(R.id.tv_title_city);
        titleUpdateTime = findViewById(R.id.tv_title_update_time);
        degreeText = findViewById(R.id.tv_degree);
        weatherInfoText = findViewById(R.id.tv_weather_info);
        forecastLayout = findViewById(R.id.layout_forecast);
        suggestionLayout = findViewById(R.id.layout_suggestion);
        aqiText = findViewById(R.id.tv_aqi);
        pm25Text = findViewById(R.id.tv_pm25);
        bingImg = findViewById(R.id.img_bing);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.btn_nav);

        //下拉刷新监听
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //下拉刷新，则直接使用远程数据源，获取最新数据
                if (mDataRepository != null) {
                    mDataRepository.loadWeatherDataFromRemoteSource(mCurrentCityName, mLoadWeatherDataCallback);
                    mDataRepository.loadAQIDataFromRemoteSource(mCurrentCityName, mLoadAQIDataCallback);
                }
            }
        });

        //选择城市菜单按钮监听
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        loadAllData(mCurrentCityName);//从缓存读数据，或 从上面请求到信息，然后通过解析、转换成weather对象，再存到缓存中

        // Glide加载背景图
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);  //缓存
        String bingPicUrl = prefs.getString("bing_pic", null);      //从缓存中获取背景图的链接
        if (bingPicUrl != null) {   //如果缓存中有背景图的链接
            Glide.with(this).load(bingPicUrl).into(bingImg);
        } else {    //缓存中没东西，就要去用api请求
            loadBingPicPathFromGuolinAPI();
        }
    }

    public void loadAllData(String cityName) {
        mCurrentCityName = cityName;
        //加载天气数据
        mDataRepository.loadWeatherData(cityName, mLoadWeatherDataCallback);
        //加载空气质量数据
        mDataRepository.loadAQIData(cityName, mLoadAQIDataCallback);
    }

    DataSource.LoadWeatherDataCallback mLoadWeatherDataCallback = new DataSource.LoadWeatherDataCallback() {
        @Override
        public void onWeatherDataLoaded(final Weather weather) {
            runOnUiThread(new Runnable() {//切到主线程
                @Override
                public void run() {
                    if (weather != null && "ok".equals(weather.status)) {//如果ok就显示出来
                        showWeatherInfo(weather);
                    } else {//否则失败
                        Toast.makeText(WeatherActivity.this,
                                "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                    loadBingPicPathFromGuolinAPI();
                }
            });
        }

        @Override
        public void onWeatherDataNotAvailable() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(WeatherActivity.this,
                            "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    };

    DataSource.LoadAQIDataCallback mLoadAQIDataCallback = new DataSource.LoadAQIDataCallback() {
        @Override
        public void onAQIDataLoaded(final AQI aqi) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (aqi != null && "ok".equals(aqi.status)) {
                        showAQIInfo(aqi);
                    } else {

                        /*Toast.makeText(WeatherActivity.this,
                                "获取空气质量信息失败1", Toast.LENGTH_SHORT).show();*/
                    }
                }
            });
        }

        @Override
        public void onAQIDataNotAvailable() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   Toast.makeText(WeatherActivity.this,
                            "获取空气质量信息失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private void showWeatherInfo(Weather weather) {
        //从weather对象中获取数据，然后显示到控件上
        titleCity.setText(weather.basic.cityName);
        titleUpdateTime.setText(weather.update.localUpdateTime.split(" ")[1]);
        degreeText.setText(weather.now.temperature + "℃");
        weatherInfoText.setText(weather.now.weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            // to learn
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.tv_date);
            TextView infoText = view.findViewById(R.id.tv_info);
            TextView maxText = view.findViewById(R.id.tv_max);
            TextView minText = view.findViewById(R.id.tv_min);
            dateText.setText(forecast.date);
            infoText.setText(forecast.weatherInfoDay);
            maxText.setText(forecast.maxTemper);
            minText.setText(forecast.minTemper);
            forecastLayout.addView(view);
        }

        for (Suggestion suggestion : weather.suggestionList) {
            // to learn
            View view = LayoutInflater.from(this).inflate(R.layout.suggestion_item, suggestionLayout, false);
            TextView indexText = view.findViewById(R.id.tv_index);
            TextView suggestionText = view.findViewById(R.id.tv_suggestion);
            indexText.setText("程度 : " + suggestion.level);
            suggestionText.setText("建议 : " + suggestion.suggestionInfo);
            suggestionLayout.addView(view);
        }
        weatherScrollView.setVisibility(View.VISIBLE);
        mCurrentCityName = weather.basic.cityName;
        // 安排天气信息更新任务，在合适的时机启动服务更新天气信息
        scheduleUpdateJob();
    }

    private void showAQIInfo(AQI aqi) {
        aqiText.setText(aqi.aqiNowInfo.aqi);
        pm25Text.setText(aqi.aqiNowInfo.pm25);
    }

    /**
     * 从郭霖提供的API获取bing的每日一图
     */
    private void loadBingPicPathFromGuolinAPI() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {     //获取必应背景图链接
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                //将链接缓存到sharepreference
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {     //将当前线程切换到主线程
                            Glide.with(WeatherActivity.this).load(bingPic).into(bingImg);   //用Glide加载这张图片
                    }
                });
            }
        });
    }

    //后台自动更新
    private void scheduleUpdateJob() {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(WeatherActivity.this, UpdateJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(1, componentName);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); //连wifi时才执行
        builder.setRequiresDeviceIdle(true); //  空闲模式时执行
        builder.setRequiresBatteryNotLow(true); // 电量充足时才执行

        builder.setPeriodic(1000 * 60 * 60 * 5); //每5小时执行一次
        //builder.setOverrideDeadline(1000 * 60 * 60 * 6); //6小时后如果不触发，则强制执行，不能与setPeriodic一起设置
        builder.setPersisted(true); //重启后任务还会生效
        scheduler.schedule(builder.build());
    }
}
