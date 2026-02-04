package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2020/7/31.
 */

@Parcelize
@Serializable
data class CommonJsonModel<T : Parcelable>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null
) : Parcelable {

    val isSuccess: Boolean get() = code == 200

    val successData: T? get() = if (isSuccess) data else null
}