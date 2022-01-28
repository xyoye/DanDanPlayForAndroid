package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/11/26.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class DanmuSearchData(
    val hasMore: Boolean,
    val animes: MutableList<DanmuAnimeData>?
) : CommonJsonData()

@Parcelize
@JsonClass(generateAdapter = true)
data class DanmuAnimeData(
    val animeId: Int,
    val animeTitle: String?,
    val type: String?,
    val episodes: MutableList<DanmuEpisodeData>?
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class DanmuEpisodeData(
    val episodeId: Int = 0,
    val episodeTitle: String = ""
) : Parcelable