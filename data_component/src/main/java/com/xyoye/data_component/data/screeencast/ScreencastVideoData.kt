package com.xyoye.data_component.data.screeencast

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import com.xyoye.data_component.enums.MediaType
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2022/9/16
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class ScreencastData(
    val port: Int = 0,
    var ip: String? = null,
    val playIndex: Int = 0,
    val mediaType: String = MediaType.OTHER_STORAGE.value,
    val httpHeader: Map<String, String>? = null,
    val videos: List<ScreencastVideoData> = emptyList(),
    val uniqueKey: String? = null,
) : Parcelable{

    fun getVideoUrl(videoIndex: Int): String {
        return "http://$ip:$port/video?index=$videoIndex"
    }

    fun getDanmuUrl(videoIndex: Int): String {
        return "http://$ip:$port/danmu?index=$videoIndex"
    }

    fun getSubtitleUrl(videoIndex: Int): String {
        return "http://$ip:$port/subtitle?index=$videoIndex"
    }
}

@Parcelize
@JsonClass(generateAdapter = true)
data class ScreencastVideoData(
    val videoIndex: Int = 0,
    val videoTitle: String = ""
) : Parcelable