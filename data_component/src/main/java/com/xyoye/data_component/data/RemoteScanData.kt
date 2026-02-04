package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Created by xyoye on 2021/3/25.
 */

@Parcelize
@Serializable
data class RemoteScanData(
    val ip: List<String> = emptyList(),
    val port: Int = 0,
    val machineName: String = "",
    val tokenRequired: Boolean = false,

    @Transient
    var selectedIP: String? = null
) : Parcelable