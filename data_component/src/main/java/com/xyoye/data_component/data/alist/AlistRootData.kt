package com.xyoye.data_component.data.alist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2024/1/20.
 */

@Parcelize
@Serializable
data class AlistRootData(
    @SerialName("username")
    val userName: String = ""
) : Parcelable
