package com.xyoye.data_component.data

import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2021/1/6.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class LoginData(
    val userName: String?,
    val token: String?,
    var screenName: String?,
    val profileImage: String?
) : CommonJsonData()