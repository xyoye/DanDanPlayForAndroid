package com.xyoye.data_component.data.screeencast

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2022/9/16
 */

@Parcelize
@Serializable
data class ScreencastData(
    val port: Int,
    val relatedVideos: List<ScreencastVideoData>,
    val playUniqueKey: String,
    val httpHeader: Map<String, String>? = null,
) : Parcelable {

    @IgnoredOnParcel
    var ip: String? = null
}

@Parcelize
@Serializable
data class ScreencastVideoData(
    val title: String = "",
    val uniqueKey: String = "",
    val episodeId: String? = null,
    val danmuFileName: String? = null,
    val subtitleFileName: String? = null,
    val position: Long = 0L,
    val duration: Long = 0L
) : Parcelable