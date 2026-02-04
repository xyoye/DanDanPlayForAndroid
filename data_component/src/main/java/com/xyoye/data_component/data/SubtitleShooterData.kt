package com.xyoye.data_component.data

import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2020/11/30.
 */

@Serializable
data class SubtitleShooterData(
    val Files: List<ShooterData> = emptyList()
)

@Serializable
data class ShooterData(
    val Ext: String? = null,
    val Link: String? = null
)