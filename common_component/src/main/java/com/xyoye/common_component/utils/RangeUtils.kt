package com.xyoye.common_component.utils

import okhttp3.internal.toLongOrDefault

object RangeUtils {

    fun getRange(rangeText: String, contentLength: Long): Array<Long> {
        val rangArray = Array<Long>(3) { 0 }

        val range = parseRange(rangeText, contentLength)
            ?: return rangArray

        rangArray[0] = range.first
        rangArray[1] = range.second
        rangArray[2] = contentLength
        return rangArray
    }

    /**
     * 解析Range请求头的值
     */
    fun parseRange(rangeValue: String, contentLength: Long): Pair<Long, Long>? {
        if (contentLength <= 0) {
            return null
        }

        val maxRange = contentLength - 1
        val separator = "-"
        val header = "bytes="
        val range = rangeValue.replace(header, "")

        // e.g. "" or "-"
        if (range.length < 2) {
            return null
        }
        // e.g. "abc"
        val separatorIndex = range.indexOf(separator)
        if (separatorIndex == -1) {
            return null
        }
        // e.g. "-499"
        if (separatorIndex == 0) {
            val end = range.substring(1).toLongOrDefault(0L)
            return if (end <= 0 || end > maxRange) {
                null
            } else {
                0L to end
            }
        }
        // e.g. "500-"
        if (separatorIndex == range.length - 1) {
            val start = range.substring(0, separatorIndex).toLongOrDefault(0L)
            return if (start < 0) {
                null
            } else {
                start to maxRange
            }
        }
        // e.g. "500-999"
        val start = range.substring(0, separatorIndex).toLongOrDefault(0L)
        val end = range.substring(separatorIndex + 1).toLongOrDefault(0L)
        return if (start < 0 || end <= 0 || end > maxRange || start >= end) {
            null
        } else {
            start to end
        }
    }
}