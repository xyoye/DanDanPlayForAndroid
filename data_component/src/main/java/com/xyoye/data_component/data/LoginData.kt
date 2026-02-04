package com.xyoye.data_component.data

import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2021/1/6.
 */

@Parcelize
@Serializable
data class LoginData(
    val userName: String = "",
    val token: String = "",
    var screenName: String = "",
    val profileImage: String = ""
) : CommonJsonData()