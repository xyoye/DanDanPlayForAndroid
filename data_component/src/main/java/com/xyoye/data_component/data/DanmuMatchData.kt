package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/11/23.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class DanmuMatchData(
    val isMatched: Boolean,
    val matches: MutableList<DanmuMatchDetailData>? = null
) : CommonJsonData()

@Parcelize
@JsonClass(generateAdapter = true)
data class DanmuMatchDetailData(
    val episodeId: Int,
    val animeId: Int,
    val animeTitle: String?,
    val episodeTitle: String?,
    val type: String?,
    val shift: Int
) : Parcelable