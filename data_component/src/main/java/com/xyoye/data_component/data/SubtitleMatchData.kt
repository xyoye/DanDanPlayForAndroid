package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2018/12/19.
 */

@JsonClass(generateAdapter = true)
data class SubtitleMatchData(
    var name: String,
    var url: String,
    var origin: String,
    var rate: Int = 0
)