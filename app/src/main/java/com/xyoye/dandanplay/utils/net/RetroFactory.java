package com.xyoye.dandanplay.utils.net;

import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.net.gson.GsonFactory;
import com.xyoye.dandanplay.utils.net.okhttp.OkHttpEngine;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by xyoye on 2018/7/9.
 */


public class RetroFactory {
    private static final String url = "https://api.acplay.net/";
    private static final String resUrl = "http://res.acplay.net/";
    private static final String downloadUrl = "https://m2t.chinacloudsites.cn/";
    private static final String subtitleUrl = "https://dandanplay.com/";

    private static RetrofitService apiRetrofitService;
    private static RetrofitService resRetrofitService;
    private static RetrofitService subtitleRetrofitService;
    private static RetrofitService downloadTorrentRetrofitService;

    private RetroFactory() {

    }

    public static RetrofitService getInstance(){
        if (apiRetrofitService == null){
            apiRetrofitService = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create(GsonFactory.buildGson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(initOkHttp())
                    .build()
                    .create(RetrofitService.class);
        }
        return apiRetrofitService;
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

    public static RetrofitService getDTInstance(){
        if (downloadTorrentRetrofitService == null){
            downloadTorrentRetrofitService = new Retrofit.Builder()
                    .baseUrl(downloadUrl)
                    .addConverterFactory(GsonConverterFactory.create(GsonFactory.buildGson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(initNoHeaderOkHttp())
                    .build()
                    .create(RetrofitService.class);
        }
        return downloadTorrentRetrofitService;
    }

    public static RetrofitService getSubtitleInstance(){
        if (subtitleRetrofitService == null){
            subtitleRetrofitService = new Retrofit.Builder()
                    .baseUrl(subtitleUrl)
                    .addConverterFactory(GsonConverterFactory.create(GsonFactory.buildGson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(initSubtitleOkHttp())
                    .build()
                    .create(RetrofitService.class);
        }
        return subtitleRetrofitService;
    }

    private static OkHttpClient initOkHttp() {
        return OkHttpEngine.getInstance()
                .getOkHttpClient()
                .newBuilder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .addInterceptor(new GzipInterceptor())
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
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

    }

    private static OkHttpClient initSubtitleOkHttp() {
        return OkHttpEngine.getInstance()
                .getOkHttpClient()
                .newBuilder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
                    Request oldRequest = chain.request();
                    Request.Builder newRequest = oldRequest.newBuilder();
                    List<String> headerValues = oldRequest.headers("query");
                    HttpUrl newBaseUrl;

                    if (headerValues.size() > 0 && "shooter".equals(headerValues.get(0))){
                        newRequest.removeHeader("query");
                        newBaseUrl = HttpUrl.parse("https://www.shooter.cn/");
                    }else {
                        newRequest.removeHeader("query");
                        newBaseUrl = HttpUrl.parse("http://sub.xmp.sandai.net:8000/");
                    }

                    if (newBaseUrl != null) {
                        HttpUrl newUrl = oldRequest.url()
                                .newBuilder()
                                .scheme(newBaseUrl.scheme())
                                .host(newBaseUrl.host())
                                .port(newBaseUrl.port())
                                .build();
                        return chain.proceed(newRequest.url(newUrl).build());
                    }
                    return chain.proceed(oldRequest);
                })
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

    }
}
