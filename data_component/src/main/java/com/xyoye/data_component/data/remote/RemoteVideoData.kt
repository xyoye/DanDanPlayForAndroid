package com.xyoye.data_component.data.remote

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import com.xyoye.data_component.helper.NullToEmptyString
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2021/3/28.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteVideoData(
    val AnimeId: Int = 0,
    @NullToEmptyString
    val AnimeTitle: String = "",
    val Duration: Long = 0,
    @NullToEmptyString
    val EpisodeTitle: String = "",
    @NullToEmptyString
    val Hash: String = "",
    @NullToEmptyString
    val Id: String = "",
    val IsStandalone: Boolean = false,
    @NullToEmptyString
    val Name: String = "",
    @NullToEmptyString
    val Path: String = "",
    val Size: Long = 0,

    @NullToEmptyString
    var absolutePath: String = "",
    var isFolder: Boolean = false,
    var childData: MutableList<RemoteVideoData> = mutableListOf()
) : Parcelable {

    fun getEpisodeName(): String{
        return EpisodeTitle.ifEmpty { Name }
    }
}