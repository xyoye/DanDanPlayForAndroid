package com.xyoye.common_component.network.repository

import com.xyoye.common_component.network.Retrofit

/**
 * Created by xyoye on 2024/1/11.
 */

object MagnetRepository : BaseRepository() {

    /**
     * 获取磁链资源分类
     */
    suspend fun getMagnetType(domain: String) = request()
        .doGet {
            Retrofit.magnetService.getMagnetType(domain)
        }

    /**
     * 获取磁链资源字幕组
     */
    suspend fun getMagnetSubgroup(domain: String) = request()
        .doGet {
            Retrofit.magnetService.getMagnetSubgroup(domain)
        }

    /**
     * 搜索磁链资源
     */
    suspend fun searchMagnet(
        domain: String,
        keyword: String,
        type: String,
        subgroup: String
    ) = request()
        .param("keyword", keyword)
        .param("type", type)
        .param("subgroup", subgroup)
        .doGet {
            Retrofit.magnetService.searchMagnet(domain, it)
        }
}