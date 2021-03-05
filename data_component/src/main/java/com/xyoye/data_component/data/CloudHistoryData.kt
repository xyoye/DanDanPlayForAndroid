package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2021/1/9.
 */

@Parcelize
data class CloudHistoryListData(
    val playHistoryAnimes: MutableList<CloudHistoryData> = mutableListOf()
): CommonJsonData(), Parcelable

@Parcelize
data class CloudHistoryData(
    val animeId: Int,
    val animeTitle: String?,
    val type: String?,
    val typeDescription: String?,
    val imageUrl: String?,
    val isOnAir: Boolean,
    val episodes: MutableList<CloudHistoryEpisodeData> = mutableListOf()
): Parcelable

@Parcelize
data class CloudHistoryEpisodeData(
    val episodeId: Int,
    val episodeTitle: String?,
    val episodeNumber: String?,
    val lastWatched: String?,
    val airDate: String?
): Parcelable