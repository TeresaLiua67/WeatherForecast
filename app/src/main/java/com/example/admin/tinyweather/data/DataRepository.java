package com.example.admin.tinyweather.data;



import com.example.admin.tinyweather.gson.AQI;
import com.example.admin.tinyweather.gson.Weather;
import com.example.admin.tinyweather.util.Utility;

/**
 * 用来管理各种类型的DataSource的调用时机，和配合方式等等，相当于一个调度管理中心。
 * 现在有了本地数据源LocalDataSource和远程数据源RemoteDataSource, 甚至以后还有可能有更多数据源，那
 * 什么时候使用本地数据源，什么时候使用远程数据源，总要有个逻辑来判断，这个逻辑可以写在Activity中，也
 * 可以把逻辑抽出来写在这个类中。
 */
public class DataRepository implements DataSource {
    private static DataRepository sDataRepository = null;

    private DataSource mRemoteDataSource;

    private DataSource mLocalDataSource;

    private DataRepository (DataSource remoteDataSource, DataSource localDataSource) {
        mRemoteDataSource = remoteDataSource;
        mLocalDataSource = localDataSource;
    }

    /**
     * 饿汉单例模式，返回一个唯一对象，在此不考虑多线程问题
     * @param remoteDataSource
     * @param localDataSource
     * @return
     */
    public static DataRepository getInstance(DataSource remoteDataSource, DataSource localDataSource) {
        if (sDataRepository == null) {
            sDataRepository = new DataRepository(remoteDataSource, localDataSource);
        }
        return sDataRepository;
    }

    @Override
    public void loadWeatherData(final String id, final LoadWeatherDataCallback callback) {
        //先从本地数据源获取天气数据
        mLocalDataSource.loadWeatherData(id, new LoadWeatherDataCallback() {
            @Override
            public void onWeatherDataLoaded(Weather weather) {
                callback.onWeatherDataLoaded(weather);
            }

            @Override
            public void onWeatherDataNotAvailable() { // 如果本地数据源无数据，再从远程数据源获取数据
                loadWeatherDataFromRemoteSource(id, callback);
            }
        });
    }

    @Override
    public void loadAQIData(final String id, final LoadAQIDataCallback callback) {
        //aqi 数据暂时全部从网络获取，不存储在本地
        loadAQIDataFromRemoteSource(id, callback);
    }

    @Override
    public void saveWeatherData(String city, String weatherString) {
        mLocalDataSource.saveWeatherData(city, weatherString);
    }

    @Override
    public void saveAQIData(String city, String aqiString) {
        mLocalDataSource.saveAQIData(city, aqiString);
    }

    /**
     * 直接从远程数据源获取天气数据
     * @param id
     * @param callback
     */
    public void loadWeatherDataFromRemoteSource(final String id, final LoadWeatherDataCallback callback) {
        mRemoteDataSource.loadWeatherData(id, new LoadWeatherDataCallback() {
            @Override
            public void onWeatherDataLoaded(Weather weather) {
                callback.onWeatherDataLoaded(weather);
                // 存起来，方便以后使用
                mLocalDataSource.saveWeatherData(id, Utility.toJson(weather));
            }

            @Override
            public void onWeatherDataNotAvailable() {
                callback.onWeatherDataNotAvailable();
            }
        });
    }

    /**
     * 直接从远程数据源获取空气质量数据
     * @param id
     * @param callback
     */
    public void loadAQIDataFromRemoteSource(final String id, final LoadAQIDataCallback callback) {
        mRemoteDataSource.loadAQIData(id, new LoadAQIDataCallback() {
            @Override
            public void onAQIDataLoaded(AQI aqi) {
                callback.onAQIDataLoaded(aqi);
            }

            @Override
            public void onAQIDataNotAvailable() {
                callback.onAQIDataNotAvailable();
            }
        });
    }
}
