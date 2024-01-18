package com.xyoye.common_component.utils.danmu.query

import com.xyoye.data_component.data.DanmuAnimeData
import com.xyoye.data_component.data.DanmuContentData
import com.xyoye.data_component.data.DanmuEpisodeData
import com.xyoye.data_component.data.DanmuRelatedUrlData

/**
 * Created by xyoye on 2024/1/14.
 */

interface DanmuQuery {

    companion object {
        val instance: DanmuQueryImpl by lazy {
            DanmuQueryImpl()
        }
    }

    /**
     * 匹配弹幕
     */
    suspend fun match(hash: String): DanmuEpisodeData?

    /**
     * 搜索弹幕
     */
    suspend fun search(text: String): List<DanmuAnimeData>

    /**
     * 获取弹幕源
     */
    suspend fun source(episodeId: String): List<DanmuRelatedUrlData>

    /**
     * 获取弹幕内容，根据剧集ID
     */
    suspend fun getContentByEpisodeId(episodeId: String, withRelated: Boolean = true): List<DanmuContentData>

    /**
     * 获取弹幕内容，根据资源链接
     */
    suspend fun getContentByUrl(url: String): List<DanmuContentData>
}