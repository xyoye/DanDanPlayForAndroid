package com.xyoye.data_component.data.remote

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2021/3/28.
 */

@JsonClass(generateAdapter = true)
data class RemoteVideoData(
    val AnimeId: Int,
    val AnimeTitle: String?,
    val Created: String,
    val Duration: Long,
    val EpisodeId: Int,
    val EpisodeTitle: String?,
    val Hash: String,
    val Id: String,
    val IsStandalone: Boolean,
    val Name: String,
    val Path: String,
    val Size: Long
)