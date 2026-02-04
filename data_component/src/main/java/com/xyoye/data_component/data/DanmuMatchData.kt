package com.xyoye.data_component.data

import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2020/11/23.
 */

@Parcelize
@Serializable
data class DanmuMatchData(
    val isMatched: Boolean = false,
    val matches: List<DanmuEpisodeData> = emptyList()
) : CommonJsonData()