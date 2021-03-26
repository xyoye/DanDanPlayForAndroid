package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2020/10/26.
 */

@JsonClass(generateAdapter = true)
data class MagnetTypeData(
    val Types: MutableList<MagnetScreenData> = mutableListOf()
)