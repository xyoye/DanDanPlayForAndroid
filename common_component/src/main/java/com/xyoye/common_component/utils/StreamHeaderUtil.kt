package com.xyoye.common_component.utils

/**
 * Created by xyoye on 2021/1/22.
 */

object StreamHeaderUtil {

    fun string2Header(text: String?): Map<String, String>? {
        if (text.isNullOrEmpty())
            return null

        //未输入分号，且仅有一个请求头时
        if (!text.contains(";")) {
            if (text.contains(":")) {
                val keyValue = text.split(":".toRegex())
                if (keyValue.size == 2) {
                    return mapOf(Pair(keyValue[0], keyValue[1]))
                }
            }
            return null
        }

        val header = hashMapOf<String, String>()

        val headers = text.split(";".toRegex())
        headers.forEach {
            if (it.isNotEmpty()) {
                if (text.contains(":")) {
                    val keyValue = text.split(":".toRegex())
                    if (keyValue.size == 2) {
                        header[keyValue[0]] = keyValue[1]
                    }
                }
            }
        }
        return header
    }

    fun header2String(header: Map<String, String>?): String? {
        if (header == null)
            return null

        val headerStr = StringBuilder()
        header.entries.forEach {
            headerStr.append(it.key)
                .append(":")
                .append(it.value)
                .append(";")
        }

        if (headerStr.isNotEmpty()) {
            return headerStr.substring(0, headerStr.length - 1)
        }
        return null
    }
}