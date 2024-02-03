package com.xyoye.common_component.utils.danmu.query

import com.xyoye.common_component.network.repository.ResourceRepository

import com.xyoye.data_component.data.DanmuAnimeData
import com.xyoye.data_component.data.DanmuContentData
import com.xyoye.data_component.data.DanmuEpisodeData
import com.xyoye.data_component.data.DanmuRelatedUrlData

/**
 * Created by xyoye on 2024/1/14.
 */

class DanmuQueryImpl : DanmuQuery {

    override suspend fun match(hash: String): DanmuEpisodeData? {
        val result = ResourceRepository.matchDanmu(hash).getOrNull()
            ?: return null

        if (result.isMatched.not()) {
            return null
        }
        return result.matches.firstOrNull()
    }

    override suspend fun search(text: String): List<DanmuAnimeData> {
        return ResourceRepository.searchDanmu(text).getOrNull()
            ?.animes
            ?.filter { it.episodes.isNotEmpty() }
            ?.map { anime ->
                anime.copy(
                    episodes = anime.episodes.map {
                        it.copy(animeId = anime.animeId, animeTitle = anime.animeTitle)
                    }
                )
            }
            ?: emptyList()
    }

    override suspend fun source(episodeId: String): List<DanmuRelatedUrlData> {
        return ResourceRepository.getRelatedDanmu(episodeId).getOrNull()
            ?.relateds
            ?: emptyList()
    }

    override suspend fun getContentByEpisodeId(episodeId: String, withRelated: Boolean): List<DanmuContentData> {
        return ResourceRepository.getDanmuContent(episodeId, withRelated).getOrNull()
            ?.comments
            ?: emptyList()
    }

    override suspend fun getContentByUrl(url: String): List<DanmuContentData> {
        return ResourceRepository.getRelatedDanmuContent(url).getOrNull()
            ?.comments
            ?: emptyList()
    }
}