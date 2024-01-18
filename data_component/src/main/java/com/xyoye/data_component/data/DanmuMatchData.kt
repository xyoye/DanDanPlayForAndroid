package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/11/23.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class DanmuMatchData(
    val isMatched: Boolean,
    val matches: List<DanmuEpisodeData> = emptyList()
) : CommonJsonData()