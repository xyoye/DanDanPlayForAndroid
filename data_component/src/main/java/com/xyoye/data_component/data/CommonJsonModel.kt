package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/7/31.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class CommonJsonModel<T : Parcelable>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null
) : Parcelable {

    val isSuccess: Boolean get() = code == 200

    val successData: T? get() = if (isSuccess) data else null
}