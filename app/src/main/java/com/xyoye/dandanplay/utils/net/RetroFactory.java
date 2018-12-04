package com.xyoye.dandanplay.utils.net;

import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.net.gson.GsonFactory;
import com.xyoye.dandanplay.utils.net.okhttp.OkHttpEngine;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by YE on 2018/7/9.
 */


public class RetroFactory {
    private static String url = "https://api.acplay.net/";
    private static String resUrl = "http://res.acplay.net/";
    private static String downloadUrl = " https://m2t.chinacloudsites.cn/";

    private static RetrofitService retrofitService;
    private static RetrofitService resRetrofitService;
    private static RetrofitService downloadRetrofitService;

    private RetroFactory() {

    }

    public static RetrofitService getInstance(){
        if (retrofitService == null){
            retrofitService = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create(GsonFactory.buildGson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(initOkHttp())
                    .build()
                    .create(RetrofitService.class);
        }
        return retrofitService;
    }

    public static RetrofitService getResInstance(){
        if (resRetrofitService == null){
            resRetrofitService = new Retrofit.Builder()
                    .baseUrl(resUrl)
                    .addConverterFactory(GsonConverterFactory.create(GsonFactory.buildGson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(initNoHeaderOkHttp())
                    .build()
                    .create(RetrofitService.class);
        }
        return resRetrofitService;
    }

    public static RetrofitService getDownloadInstance(){
        if (downloadRetrofitService == null){
            downloadRetrofitService = new Retrofit.Builder()
                    .baseUrl(downloadUrl)
                    .addConverterFactory(GsonConverterFactory.create(GsonFactory.buildGson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(initNoHeaderOkHttp())
                    .build()
                    .create(RetrofitService.class);
        }
        return downloadRetrofitService;
    }

    private static OkHttpClient initOkHttp() {
        return OkHttpEngine.getInstance()
                .getOkHttpClient()
                .newBuilder()
                .connectTimeout(5000, TimeUnit.SECONDS)
                .readTimeout(5000, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder()
                            .header("Authorization", "Bearer "+ AppConfig.getInstance().getToken());
                    return chain.proceed(builder.build());
                })
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

    }

    private static OkHttpClient initNoHeaderOkHttp() {
        return OkHttpEngine.getInstance()
                .getOkHttpClient()
                .newBuilder()
                .connectTimeout(5000, TimeUnit.SECONDS)
                .readTimeout(5000, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

    }
}
