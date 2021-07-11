package com.xyoye.data_component.data.remote

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2021/3/28.
 */

@Parcelize
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
) : Parcelable