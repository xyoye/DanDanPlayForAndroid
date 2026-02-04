package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Created by xyoye on 2020/11/26.
 */

@Parcelize
@Serializable
data class DanmuSearchData(
    val hasMore: Boolean = false,
    val animes: List<DanmuAnimeData> = emptyList()
) : CommonJsonData()

@Parcelize
@Serializable
data class DanmuAnimeData(
    val animeId: Int = 0,
    val animeTitle: String = "",
    val episodes: List<DanmuEpisodeData> = emptyList(),

    @Transient
    val isRecommend: Boolean = false,
    @Transient
    val isBound: Boolean = false,
    @Transient
    val isSelected: Boolean = false,
) : Parcelable

@Parcelize
@Serializable
data class DanmuEpisodeData(
    val animeId: Int = 0,
    val animeTitle: String = "",
    val episodeId: String = "",
    val episodeTitle: String = "",

    @Transient
    val isRecommend: Boolean = false,
    @Transient
    val isBound: Boolean = false
) : Parcelable
