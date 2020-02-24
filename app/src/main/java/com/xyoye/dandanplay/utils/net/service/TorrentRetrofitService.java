package com.xyoye.dandanplay.utils.net.service;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by xyoye on 2020/2/23.
 */

public interface TorrentRetrofitService {

    @POST("/Magnet/Parse")
    Observable<ResponseBody> downloadTorrent(@Body RequestBody requestBody);
}
