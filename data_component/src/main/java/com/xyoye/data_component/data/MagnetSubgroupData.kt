package com.xyoye.data_component.data

import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2020/10/26.
 */

@Serializable
data class MagnetSubgroupData(
    val Subgroups: List<MagnetScreenData> = emptyList()
)