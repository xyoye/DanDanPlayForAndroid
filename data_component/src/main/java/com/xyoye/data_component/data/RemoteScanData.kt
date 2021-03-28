package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2021/3/25.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class RemoteScanData(
    val ip: List<String>,
    val port: Int,
    val machineName: String?,
    val tokenRequired: Boolean,

    var selectedIP: String?
) : Parcelable