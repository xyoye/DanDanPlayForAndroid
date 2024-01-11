package com.xyoye.common_component.network.repository

import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.data.screeencast.ScreencastData

/**
 * Created by xyoye on 2024/1/11.
 */

object ScreencastRepository : BaseRepository() {

    /**
     * 投屏初始化
     */
    suspend fun init(url: String, authorization: String?) = request()
        .doGet {
            Retrofit.screencastService.init(url, authorization)
        }

    /**
     * 投屏播放
     */
    suspend fun play(
        url: String,
        authorization: String?,
        data: ScreencastData
    ) = request()
        .json(JsonHelper.toJson(data).orEmpty())
        .doPost {
            Retrofit.screencastService.play(url, authorization, it)
        }
}