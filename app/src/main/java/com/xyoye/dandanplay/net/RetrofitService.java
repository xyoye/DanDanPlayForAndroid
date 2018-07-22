package com.xyoye.dandanplay.net;

import com.xyoye.dandanplay.bean.AnimaBeans;
import com.xyoye.dandanplay.bean.AnimaDetailBean;
import com.xyoye.dandanplay.bean.BannerBeans;
import com.xyoye.dandanplay.bean.DanmuDownloadBean;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.bean.params.LoginParam;

import java.util.Map;
import java.util.Observer;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by YE on 2018/7/9.
 */


public interface RetrofitService {

    @FormUrlEncoded
    @POST("api/v2/match")
    Observable<DanmuMatchBean> matchDanmu(@FieldMap Map<String, String> params);

    @GET("api/v2/comment/{episodeId}")
    Observable<DanmuDownloadBean> downloadDanmu(@Path("episodeId") String episodeId);

    @GET("api/v2/homepage/banner")
    Observable<BannerBeans> getBanner();

    @GET("api/v2/bangumi/shin")
    Observable<AnimaBeans> getAnimas();

    @GET("api/v2/bangumi/{animeId}")
    Observable<AnimaDetailBean> getAnimaDetail(@Path("animeId") String animaId);

    @FormUrlEncoded
    @POST("api/v2/login")
    Observable<PersonalBean> login(@FieldMap Map<String, String> params);

    @GET("api/v2/login/renew")
    Observable<PersonalBean> reToken();
}
