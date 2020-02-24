package com.xyoye.dandanplay.utils.net.service;

import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.MagnetBean;
import com.xyoye.dandanplay.bean.SubGroupBean;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by xyoye on 2020/2/23.
 */

public interface ResRetrofitService {

    @GET("/type")
    Observable<AnimeTypeBean> getAnimeType();

    @GET("/list")
    Observable<MagnetBean> searchMagnet(@Query("keyword") String keyword, @Query("type") String typeId, @Query("subgroup") String subGroupId);

    @GET("/subgroup")
    Observable<SubGroupBean> getSubGroup();
}
