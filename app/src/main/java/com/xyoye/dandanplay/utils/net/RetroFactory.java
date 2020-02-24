package com.xyoye.dandanplay.utils.net;

import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.net.gson.GsonFactory;
import com.xyoye.dandanplay.utils.net.okhttp.OkHttpEngine;
import com.xyoye.dandanplay.utils.net.service.ResRetrofitService;
import com.xyoye.dandanplay.utils.net.service.RetrofitService;
import com.xyoye.dandanplay.utils.net.service.ShooterRetrofitService;
import com.xyoye.dandanplay.utils.net.service.SubtitleRetrofitService;
import com.xyoye.dandanplay.utils.net.service.TorrentRetrofitService;

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
    private static final String shooterUrl = "http://api.assrt.net/";

    private static RetrofitService apiRetrofitService;
    private static ResRetrofitService resRetrofitService;
    private static TorrentRetrofitService torrentRetrofitService;
    private static SubtitleRetrofitService subtitleRetrofitService;
    private static ShooterRetrofitService shooterRetrofitService;

    private RetroFactory() {

    }

    public static RetrofitService getInstance() {
        if (apiRetrofitService == null) {
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

    public static ResRetrofitService getResInstance() {
        if (resRetrofitService == null) {
            resRetrofitService = new Retrofit.Builder()
                    .baseUrl(resUrl)
                    .addConverterFactory(GsonConverterFactory.create(GsonFactory.buildGson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(initNoHeaderOkHttp())
                    .build()
                    .create(ResRetrofitService.class);
        }
        return resRetrofitService;
    }

    public static TorrentRetrofitService getDTInstance() {
        if (torrentRetrofitService == null) {
            torrentRetrofitService = new Retrofit.Builder()
                    .baseUrl(downloadUrl)
                    .addConverterFactory(GsonConverterFactory.create(GsonFactory.buildGson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(initNoHeaderOkHttp())
                    .build()
                    .create(TorrentRetrofitService.class);
        }
        return torrentRetrofitService;
    }

    public static SubtitleRetrofitService getSubtitleInstance() {
        if (subtitleRetrofitService == null) {
            subtitleRetrofitService = new Retrofit.Builder()
                    .baseUrl(subtitleUrl)
                    .addConverterFactory(GsonConverterFactory.create(GsonFactory.buildGson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(initSubtitleOkHttp())
                    .build()
                    .create(SubtitleRetrofitService.class);
        }
        return subtitleRetrofitService;
    }

    public static ShooterRetrofitService getShooterInstance() {
        if (shooterRetrofitService == null) {
            shooterRetrofitService = new Retrofit.Builder()
                    .baseUrl(shooterUrl)
                    .addConverterFactory(GsonConverterFactory.create(GsonFactory.buildGson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(initShooterOkHttp())
                    .build()
                    .create(ShooterRetrofitService.class);
        }
        return shooterRetrofitService;
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
                            .header("Authorization", "Bearer " + AppConfig.getInstance().getToken());
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

                    if (headerValues.size() > 0 && "shooter".equals(headerValues.get(0))) {
                        newRequest.removeHeader("query");
                        newBaseUrl = HttpUrl.parse("https://www.shooter.cn/");
                    } else {
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

    private static OkHttpClient initShooterOkHttp() {
        return OkHttpEngine.getInstance()
                .getOkHttpClient()
                .newBuilder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
                    Request oldRequest = chain.request();
                    Request.Builder newRequest = oldRequest.newBuilder();
                    List<String> headerValues = oldRequest.headers("download_url");
                    HttpUrl newBaseUrl = null;

                    if (headerValues.size() > 0) {
                        newRequest.removeHeader("download_url");
                        newBaseUrl = HttpUrl.parse(headerValues.get(0));
                    }

                    if (newBaseUrl != null) {
                        return chain.proceed(newRequest.url(newBaseUrl).build());
                    }
                    return chain.proceed(oldRequest);
                })
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

    }
}
