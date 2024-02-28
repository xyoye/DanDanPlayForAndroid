package com.xyoye.common_component.utils

import com.xyoye.common_component.extension.toText
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by XYJ on 2021/2/8.
 */

object PlayHistoryUtils {
    private val dayName = arrayOf("今天", "昨天", "前天")

    fun formatPlayTime(time: Date): String {
        val currTime = System.currentTimeMillis()
        if (time.time > currTime) {
            return time.toText("yyyy-MM-dd HH:mm")
        }
        // get the timestamp of today's beginning.
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dayStartDate = sdf.parse(sdf.format(currTime)) ?: return time.toText("yyyy-MM-dd HH:mm")
        var dayStart = dayStartDate.time
        // check if the given datetime matches dayName.
        var offset = 0
        while (offset < dayName.size) {
            if (dayStart <= time.time) {
                break
            }
            dayStart -= 24 * 60 * 60 * 1000
            offset++
        }
        // offset will be 3(out of index) if not matches dayName.
        return if (offset in dayName.indices) {
            dayName[offset] + " " + time.toText("HH:mm")
        } else {
            time.toText("yyyy-MM-dd HH:mm")
        }
    }
}