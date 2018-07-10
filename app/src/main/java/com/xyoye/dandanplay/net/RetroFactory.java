package com.xyoye.dandanplay.net;

import com.xyoye.core.gson.GsonFactory;
import com.xyoye.core.net.okhttp.OkHttpEngine;

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
                    .client(OkHttpEngine.getInstance().getOkHttpClient())
                    .build()
                    .create(RetrofitService.class);
        }
        return retrofitService;
    }
}
