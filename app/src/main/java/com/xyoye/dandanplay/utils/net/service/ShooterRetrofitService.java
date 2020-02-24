package com.xyoye.dandanplay.utils.net.service;

import com.xyoye.dandanplay.bean.ShooterQuotaBean;
import com.xyoye.dandanplay.bean.ShooterSubDetailBean;
import com.xyoye.dandanplay.bean.ShooterSubtitleBean;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Created by xyoye on 2020/2/23.
 */

public interface ShooterRetrofitService {

    @GET("v1/user/quota")
    Observable<ShooterQuotaBean> getQuota(@Query("token") String token);

    @GET("v1/sub/search")
    Observable<ShooterSubtitleBean> searchSubtitle(@Query("token") String token, @Query("q") String text, @Query("pos") int page);

    @GET("v1/sub/detail")
    Observable<ShooterSubDetailBean> querySubtitleDetail(@Query("token") String token, @Query("id") int subtitle);

    @GET("/")
    Observable<ResponseBody> downloadSubtitle(@Header("download_url") String downloadUrl);
}
