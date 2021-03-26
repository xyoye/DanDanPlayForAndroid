package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/10/26.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class MagnetScreenData(
    val Id: Int = -1,
    val Name: String = ""
) : Parcelable