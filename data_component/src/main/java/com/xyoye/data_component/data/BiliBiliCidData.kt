package com.xyoye.data_component.data

import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2021/2/23.
 */

@Serializable
data class BiliBiliCidData(
    val code: Int = 0,
    val data: EpisodeCidData? = null
)