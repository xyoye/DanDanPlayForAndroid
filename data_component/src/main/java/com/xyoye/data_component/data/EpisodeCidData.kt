package com.xyoye.data_component.data

import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2024/1/17.
 */

@Serializable
data class EpisodeCidData(
    val title: String = "",
    val cid: String = ""
)