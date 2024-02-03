package com.xyoye.common_component.utils

import com.xyoye.common_component.extension.toText
import java.util.Date
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Created by XYJ on 2021/2/8.
 */

object PlayHistoryUtils {
    private val dayName = arrayOf("今天", "昨天", "前天")

    fun formatPlayTime(time: Date): String {
        if (time.after(Date())) {
            return time.toText("yyyy-MM-dd HH:mm")
        }

        val playTime = time.time.toDuration(DurationUnit.MILLISECONDS)
        val currentTime = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)

        val intervalDay = currentTime.minus(playTime).toInt(DurationUnit.DAYS)
        if (intervalDay in dayName.indices) {
            return dayName[intervalDay] + " " + time.toText("HH:mm")
        }
        return time.toText("yyyy-MM-dd HH:mm")
    }

}