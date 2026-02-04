package com.xyoye.data_component.data.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2021/3/28.
 */

@Parcelize
@Serializable
data class RemoteVideoData(
    val AnimeId: Int = 0,
    val AnimeTitle: String = "",
    val Duration: Long = 0,
    val EpisodeTitle: String = "",
    val Hash: String = "",
    val Id: String = "",
    val IsStandalone: Boolean = false,
    val Name: String = "",
    val Path: String = "",
    val Size: Long = 0,

    var absolutePath: String = "",
    var isFolder: Boolean = false,
    var childData: List<RemoteVideoData> = emptyList()
) : Parcelable {

    fun getEpisodeName(): String{
        return EpisodeTitle.ifEmpty { Name }
    }
}
