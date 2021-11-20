package com.xyoye.common_component.utils

object RangeUtils {

    fun getRange(rangeHeader: String, contentLength: Long): Array<Long> {
        val rangArray = Array<Long>(3) { 0 }

        var targetStart = 0L
        var targetEnd = contentLength
        var targetLength = contentLength
        if (contentLength > 0) {
            val rangePair = parseRange(rangeHeader)
            if (rangePair.first < contentLength) {
                targetStart = rangePair.first
            }
            if (rangePair.second in 1..contentLength) {
                targetEnd = rangePair.second
            }
            if (targetStart < targetEnd) {
                targetLength = targetEnd - targetStart + 1
            } else {
                targetEnd = contentLength - 1
            }

            rangArray[0] = targetStart
            rangArray[1] = targetEnd
            rangArray[2] = targetLength
        }
        return rangArray
    }

    private fun parseRange(rangeText: String): Pair<Long, Long> {
        var range = rangeText.replace("bytes=", "")
        if (range.contains("-")) {
            if (range.startsWith("-")) {
                //-123
                range = "0$range"
            } else if (range.endsWith("-")) {
                //123-
                range += "0"
            }
            val ranges = range.split("-")
            if (ranges.size == 2) {
                try {
                    return Pair(ranges[0].toLong(), ranges[1].toLong())
                } catch (ignore: NumberFormatException) {

                }
            }
        }
        return Pair(0, 0)
    }
}