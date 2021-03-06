package com.xyoye.data_component.data

/**
 * Created by xyoye on 2020/11/30.
 */

data class SubtitleShooterData(
    val Files: MutableList<ShooterData>?
)

data class ShooterData(
    val Ext: String?,
    val Link: String?
)