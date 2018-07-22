package com.xyoye.dandanplay.net;

import com.xyoye.core.gson.GsonFactory;
import com.xyoye.core.net.okhttp.OkHttpEngine;
import com.xyoye.dandanplay.utils.TokenShare;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
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
    public static String url = "https://api.acplay.net/";

    private static RetrofitService retrofitService;

    private RetroFactory() {

    }

    public static  RetrofitService getInstance(){
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


    private static OkHttpClient initOkHttp() {
        return OkHttpEngine.getInstance()
                .getOkHttpClient()
                .newBuilder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder()
                            .header("Authorization", "Bearer "+TokenShare.getInstance().getToken());
                    return chain.proceed(builder.build());
                })
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

    }
}
