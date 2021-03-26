package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass

/**
 * Created by xyoye on 2020/11/30.
 */

@JsonClass(generateAdapter = true)
data class SubtitleShooterData(
    val Files: MutableList<ShooterData>?
)

@JsonClass(generateAdapter = true)
data class ShooterData(
    val Ext: String?,
    val Link: String?
)