package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/11/26.
 */

@Parcelize
data class DanmuSearchData(
    val hasMore: Boolean,
    val animes: MutableList<DanmuAnimeData>?
) : CommonJsonData()

@Parcelize
data class DanmuAnimeData(
    val animeId: Int,
    val animeTitle: String?,
    val type: String?,
    val episodes: MutableList<DanmuEpisodeData>?
) : Parcelable

@Parcelize
data class DanmuEpisodeData(
    val episodeId: Int,
    val episodeTitle: String?
) : Parcelable