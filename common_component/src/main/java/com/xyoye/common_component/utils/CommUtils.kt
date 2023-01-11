package com.xyoye.common_component.utils

import android.content.res.Resources
import java.text.Collator
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Created by xyoye on 2020/7/29.
 */

/**
 * 单位转换，将DP转为PX
 */
fun dp2px(dpValue: Float): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

/**
 * 单位转换，将DP转为PX
 */
fun dp2px(dpValue: Int): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

/**
 * 时间格式化
 */
fun formatDuration(millis: Long): String {
    val milliseconds = millis.toDuration(DurationUnit.MILLISECONDS)
    val minute = milliseconds.toInt(DurationUnit.MINUTES)
    val second = milliseconds.minus(milliseconds.inWholeMinutes.minutes).toInt(DurationUnit.SECONDS)
    return String.format("%02d:%02d", minute, second)
}

/**
 * 字符串比较
 */
fun stringCompare(str1: String?, str2: String?): Int {
    return if (str1 == null && str2 == null)
        0
    else if (str1 == null)
        -1
    else if (str2 == null)
        1
    else
        Collator.getInstance(Locale.CHINESE).compare(str1, str2)
}

fun getDomainFormUrl(url: String): String {
    val regex = Regex("(?<=://)[a-zA-Z.0-9]+(?=/)")
    val result = regex.find(url)
    return result?.value ?: url
}