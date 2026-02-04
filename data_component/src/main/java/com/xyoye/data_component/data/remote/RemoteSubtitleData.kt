package com.xyoye.data_component.data.remote

import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2021/3/28.
 */

@Serializable
data class RemoteSubtitleData (
    val subtitles: List<RemoteSubtitle> = emptyList()
)

@Serializable
data class RemoteSubtitle(
    val fileName: String = "",
    val fileSize: Long = 0
)