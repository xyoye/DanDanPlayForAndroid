package com.xyoye.common_component.utils.danmu

import com.xyoye.common_component.utils.danmu.query.DanmuQuery
import com.xyoye.common_component.utils.danmu.source.DanmuSource
import com.xyoye.data_component.bean.LocalDanmuBean
import com.xyoye.data_component.data.DanmuAnimeData
import com.xyoye.data_component.data.DanmuEpisodeData
import com.xyoye.data_component.data.DanmuRelatedUrlData
import java.io.InputStream

/**
 * Created by xyoye on 2024/1/14.
 */

interface DanmuFinder {

    companion object {
        val instance: DanmuFinder by lazy {
            DanmuFinderImpl(DanmuQuery.instance)
        }
    }

    /**
     * 获取弹幕匹配数据
     */
    suspend fun getMatched(source: DanmuSource): DanmuAnimeData?

    /**
     * 获取弹幕匹配数据，并下载
     */
    suspend fun downloadMatched(source: DanmuSource): LocalDanmuBean?

    /**
     * 下载剧集对应的官方弹幕
     */
    suspend fun downloadEpisode(episode: DanmuEpisodeData, withRelated: Boolean = true): LocalDanmuBean?

    /**
     * 下载剧集对应的第三方弹幕
     */
    suspend fun downloadRelated(episode: DanmuEpisodeData, related: List<DanmuRelatedUrlData>): LocalDanmuBean?

    /**
     * 搜索弹幕
     */
    suspend fun search(text: String): List<DanmuAnimeData>

    /**
     * 获取弹幕的第三方资源
     */
    suspend fun getRelated(episodeId: Int): List<DanmuRelatedUrlData>

    /**
     * 保存剧集对应的字幕流数据
     */
    suspend fun saveStream(episode: DanmuEpisodeData, inputStream: InputStream): LocalDanmuBean?
}