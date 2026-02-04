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
data class AlistFileData(
    val name: String = "",

    @SerialName("is_dir")
    val isDirectory: Boolean = false,

    @SerialName("raw_url")
    val rawUrl: String = "",

    val thumb: String = "",

    val size: Long = 0L

) : Parcelable
