package com.xyoye.common_component.extension

/**
 * Created by xyoye on 2023/12/27
 */

fun ByteArray.toHexString(): String {
    val result = StringBuilder(2 * size)
    for (byte in this) {
        val hexInt = byte.toInt() and 0xff
        var hexStr = Integer.toHexString(hexInt)
        if (hexStr.length < 2) {
            hexStr = "0$hexStr"
        }
        result.append(hexStr)
    }
    return result.toString()
}