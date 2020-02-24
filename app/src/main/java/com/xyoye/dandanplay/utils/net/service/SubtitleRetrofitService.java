package com.xyoye.dandanplay.utils.net.service;

import com.xyoye.player.commom.bean.SubtitleBean;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by xyoye on 2020/2/23.
 */

public interface SubtitleRetrofitService {

    @FormUrlEncoded
    @Headers({"query:shooter"})
    @POST("/api/subapi.php")
    Observable<List<SubtitleBean.Shooter>> queryShooter(@FieldMap Map<String, String> map);

    @Headers({"query:thunder"})
    @GET("/subxl/{videoHash}.json")
    Observable<SubtitleBean.Thunder> queryThunder(@Path("videoHash") String videoHash);
}
