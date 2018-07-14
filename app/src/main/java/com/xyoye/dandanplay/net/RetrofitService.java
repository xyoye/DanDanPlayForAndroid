package com.xyoye.dandanplay.net;

import com.xyoye.dandanplay.bean.DanmuDownloadBean;
import com.xyoye.dandanplay.bean.DanmuMatchBean;

import java.util.Map;

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

    //使用指定的文件名、Hash、文件长度信息寻找文件可能对应的节目信息
    @FormUrlEncoded
    @POST("api/v2/match")
    Observable<DanmuMatchBean> matchDanmu(@FieldMap Map<String, String> params);

    @GET("api/v2/comment/{episodeId}")
    Observable<DanmuDownloadBean> downloadDanmu(@Path("episodeId") String episodeId);
}
