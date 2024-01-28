package com.xyoye.common_component.network.repository

import com.xyoye.common_component.network.Retrofit

/**
 * Created by xyoye on 2024/1/5
 */

object AnimeRepository : BaseRepository() {

    /**
     * 获取每周番剧
     */
    suspend fun getWeeklyAnime() = request()
        .param("filterAdultContent", true)
        .doGet {
            Retrofit.danDanService.getWeeklyAnime(it)
        }

    /**
     * 搜索番剧
     */
    suspend fun searchAnime(keyword: String, type: String?) = request()
        .param("keyword", keyword)
        .param("type", type)
        .doGet {
            Retrofit.danDanService.searchAnime(it)
        }

    /**
     * 搜索番剧，根据标签
     */
    suspend fun searchAnimeByTag(tags: List<String>) = request()
        .param("tags", tags.joinToString(","))
        .doGet {
            Retrofit.danDanService.searchAnimeByTag(it)
        }

    /**
     * 获取季度番剧
     */
    suspend fun getSeasonAnime(year: String, month: String) = request()
        .doGet {
            Retrofit.danDanService.getSeasonAnime(year, month)
        }

    /**
     * 获取番剧详情
     */
    suspend fun getAnimeDetail(animeId: String) = request()
        .doGet {
            Retrofit.danDanService.getAnimeDetail(animeId)
        }

    /**
     * 收藏番剧
     */
    suspend fun followAnime(animeId: String) = request()
        .param("animeId", animeId)
        .param("favoriteStatus", "favorited")
        .param("rating", "0")
        .doPost {
            Retrofit.danDanService.followAnime(it)
        }

    /**
     * 取消收藏番剧
     */
    suspend fun cancelFollowAnime(animeId: String) = request()
        .doDelete {
            Retrofit.danDanService.cancelFollowAnime(animeId)
        }

    /**
     * 获取已收藏的番剧
     */
    suspend fun getFollowedAnime() = request()
        .param("onlyOnAir", false)
        .doGet {
            Retrofit.danDanService.getFollowedAnime(it)
        }

    /**
     * 获取番剧播放历史
     */
    suspend fun getPlayHistory() = request()
        .doGet {
            Retrofit.danDanService.getPlayHistory()
        }

    /**
     * 添加剧集播放历史
     */
    suspend fun addEpisodePlayHistory(episodeIds: List<String>) = request()
        .param("episodeIdList", episodeIds)
        .doPost {
            Retrofit.danDanService.addPlayHistory(it)
        }
}