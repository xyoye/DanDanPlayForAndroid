package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/11/23.
 */

@Parcelize
data class DanmuData(
    var count: Int,
    val comments: MutableList<DanmuContentData> = mutableListOf()
) : Parcelable

@Parcelize
data class DanmuContentData(
    val cid: Int,
    val p: String?,
    val m: String?
) : Parcelable