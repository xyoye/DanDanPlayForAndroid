package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2021/1/9.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class CloudHistoryListData(
    val playHistoryAnimes: MutableList<CloudHistoryData> = mutableListOf()
): CommonJsonData(), Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
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
@JsonClass(generateAdapter = true)
data class CloudHistoryEpisodeData(
    val episodeId: Int,
    val episodeTitle: String?,
    val episodeNumber: String?,
    val lastWatched: String?,
    val airDate: String?
): Parcelable