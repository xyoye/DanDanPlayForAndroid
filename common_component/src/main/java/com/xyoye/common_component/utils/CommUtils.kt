package com.xyoye.common_component.utils

import android.content.res.Resources
import java.text.Collator
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

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

fun isNumber(string: String?): Boolean {
    if (string == null)
        return false
    val pattern = Pattern.compile("^-?[0-9]+")
    return pattern.matcher(string).matches()
}

/**
 * 时间格式化
 */
fun formatDuration(mss: Long): String {
    val hours = mss / (1000 * 60 * 60)
    val minutes = mss % (1000 * 60 * 60) / (1000 * 60)
    val seconds = mss % (1000 * 60) / 1000
    val stringBuilder = StringBuilder()
    if (hours != 0L) {
        if (hours < 10) stringBuilder.append("0").append(hours)
            .append(":") else stringBuilder.append(hours).append(":")
    }
    if (minutes == 0L) {
        stringBuilder.append("00:")
    } else {
        if (minutes < 10) stringBuilder.append("0").append(minutes)
            .append(":") else stringBuilder.append(minutes).append(":")
    }
    if (seconds == 0L) {
        stringBuilder.append("00")
    } else {
        if (seconds < 10) stringBuilder.append("0").append(seconds) else stringBuilder.append(
            seconds
        )
    }
    return stringBuilder.toString()
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

fun date2Str(date: Date?, pattern: String? = null): String{
    if (date == null)
        return ""
    val timeFormat = SimpleDateFormat(pattern ?: "yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return timeFormat.format(date)
}