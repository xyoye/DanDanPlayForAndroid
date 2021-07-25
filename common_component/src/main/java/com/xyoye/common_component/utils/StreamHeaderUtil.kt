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

    fun getHttpHeader(extra: Map<String, String>?): Map<String, String>? {
        if (extra == null)
            return null

        return extra.filter {
            it.key.startsWith("http_")
        }.map {
            Pair(it.key.substring(5), it.value)
        }.toMap()
    }
}