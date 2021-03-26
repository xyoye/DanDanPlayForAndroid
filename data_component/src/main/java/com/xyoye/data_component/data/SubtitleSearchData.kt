package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2020/12/1.
 */

@JsonClass(generateAdapter = true)
data class SubtitleSearchData(
    val id: Int,
    val name: String?,
    val time: String?,
    val type: String?,
    val language: String?
)