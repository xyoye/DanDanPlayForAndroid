package com.xyoye.data_component.data.remote

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2021/3/28.
 */

@JsonClass(generateAdapter = true)
data class RemoteVideoData(
    val AnimeId: Int = 0,
    val AnimeTitle: String? = null,
    val Duration: Long? = 0,
    val EpisodeTitle: String? = null,
    val Hash: String = "",
    val Id: String = "",
    val IsStandalone: Boolean = false,
    val Name: String = "",
    val Path: String = "",

    var absolutePath: String = "",
    var isFolder: Boolean = false,
    var childData: MutableList<RemoteVideoData> = mutableListOf(),

    var danmuPath: String? = null,
    var subtitlePath: String? = null
)