package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2020/11/23.
 */

@Parcelize
@Serializable
data class DanmuData(
    var count: Int = 0,
    val comments: List<DanmuContentData> = emptyList()
) : Parcelable

@Parcelize
@Serializable
data class DanmuContentData(
    val cid: Int = 0,
    val p: String = "",
    val m: String = ""
) : Parcelable