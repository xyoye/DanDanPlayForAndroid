package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/11/25.
 */

@Parcelize
data class DanmuRelatedData(
    var relateds: MutableList<DanmuRelatedUrlData> = mutableListOf()
) : CommonJsonData()

@Parcelize
data class DanmuRelatedUrlData(
    val url: String?
) : Parcelable

