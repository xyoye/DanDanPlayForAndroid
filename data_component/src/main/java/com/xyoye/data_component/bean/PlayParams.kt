package com.xyoye.data_component.bean

import android.os.Parcelable
import com.xyoye.data_component.enums.MediaType
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/10/28.
 */

@Parcelize
data class PlayParams(
    var videoPath: String,
    var videoTitle: String?,
    var danmuPath: String?,
    var subtitlePath: String?,
    var currentPosition: Long,
    var episodeId: Int,
    var mediaType: MediaType,
    var extra: Map<String, String>? = null
) : Parcelable