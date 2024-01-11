package com.xyoye.common_component.network.repository

import com.xyoye.common_component.network.Retrofit

/**
 * Created by xyoye on 2024/1/6.
 */

object OtherRepository : BaseRepository() {

    /**
     * 获取首页Banner列表
     */
    suspend fun getHomeBanner() = request()
        .doGet {
            Retrofit.danDanService.getHomeBanner()
        }

    /**
     * 获取云屏蔽数据
     */
    suspend fun getCloudFilters() = request()
        .doGet {
            Retrofit.danDanService.getCloudFilters()
        }

    /**
     * 获取分词结果
     */
    suspend fun getSegmentWords(text: String) = request()
        .param("text", text)
        .param("tasks", listOf("tok"))
        .doPost {
            Retrofit.extendedService.segmentWords(it)
        }

    /**
     * 获取B站视频cid信息
     */
    suspend fun getCidInfo(isAvCode: Boolean, id: String) = request()
        .param(if (isAvCode) "aid" else "bvid", id)
        .doGet {
            Retrofit.extendedService.getCidInfo(it)
        }
}