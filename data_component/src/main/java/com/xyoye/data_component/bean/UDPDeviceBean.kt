package com.xyoye.data_component.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/22
 *     desc  :
 * </pre>
 */

@Parcelize
@Serializable
data class UDPDeviceBean(
    var ipAddress: String? = null,
    val httpPort: Int = 0,
    val deviceName: String = "未知投屏设备",
    val needPassword: Boolean = false,
    val count: Int = 0,
) : Parcelable {
    fun getDisplayAddress(): String {
        return "http://$ipAddress:$httpPort"
    }
}