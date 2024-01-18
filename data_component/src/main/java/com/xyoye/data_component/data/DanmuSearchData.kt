package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/11/26.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class DanmuSearchData(
    val hasMore: Boolean,
    val animes: List<DanmuAnimeData> = emptyList()
) : CommonJsonData()

@Parcelize
@JsonClass(generateAdapter = true)
data class DanmuAnimeData(
    val animeId: Int = 0,
    val animeTitle: String = "",
    val episodes: List<DanmuEpisodeData> = emptyList(),

    @Json(ignore = true)
    val isRecommend: Boolean = false,
    @Json(ignore = true)
    val isBound: Boolean = false,
    @Json(ignore = true)
    val isSelected: Boolean = false,
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class DanmuEpisodeData(
    val animeId: Int = 0,
    val animeTitle: String = "",
    val episodeId: Int = 0,
    val episodeTitle: String = "",

    @Json(ignore = true)
    val isRecommend: Boolean = false,
    @Json(ignore = true)
    val isBound: Boolean = false
) : Parcelable