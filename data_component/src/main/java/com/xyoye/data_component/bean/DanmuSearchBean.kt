package com.xyoye.data_component.bean

import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2021/8/22.
 */

@Serializable
data class DanmuSearchBean(
    val episodeId: String = "",
    val animeName: String = ""
)