package com.xyoye.data_component.data.remote

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2021/3/30.
 */

@JsonClass(generateAdapter = true)
data class RemotePlayInfo(
    val AnimeTitle: String,
    val EpisodeTitle: String?,
    val Duration: Long,
    val Position: Double,
    val Seekable: Boolean,
    val Volume: Int,
    val Playing: Boolean
)