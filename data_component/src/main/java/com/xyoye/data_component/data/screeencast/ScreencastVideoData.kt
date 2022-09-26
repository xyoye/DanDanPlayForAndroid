package com.xyoye.data_component.data.screeencast

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import com.xyoye.data_component.enums.MediaType
import kotlinx.parcelize.Parcelize

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/9/16
 *     desc  :
 * </pre>
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class ScreencastData(
    val port: Int = 0,
    var ip: String? = null,
    val playIndex: Int = 0,
    val mediaType: String = MediaType.OTHER_STORAGE.value,
    val httpHeader: Map<String, String>? = null,
    val videos: List<ScreencastVideoData> = emptyList()
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

    fun getIndexVideoUrl(videoIndex: Int): String {
        return "http://$ip:$port/indexSource?index=$videoIndex"
    }
}

@Parcelize
@JsonClass(generateAdapter = true)
data class ScreencastVideoData(
    val videoIndex: Int = 0,
    val videoTitle: String = ""
) : Parcelable