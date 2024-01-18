package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2024/1/17.
 */

@JsonClass(generateAdapter = true)
data class EpisodeCidData(
    val title: String = "",
    val cid: String = ""
)