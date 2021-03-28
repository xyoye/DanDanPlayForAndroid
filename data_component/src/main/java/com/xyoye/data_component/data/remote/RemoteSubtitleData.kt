package com.xyoye.data_component.data.remote

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2021/3/28.
 */

@JsonClass(generateAdapter = true)
data class RemoteSubtitleData (
    val subtitles: MutableList<RemoteSubtitle> = mutableListOf()
)

@JsonClass(generateAdapter = true)
data class RemoteSubtitle(
    val fileName: String,
    val fileSize: Long
)