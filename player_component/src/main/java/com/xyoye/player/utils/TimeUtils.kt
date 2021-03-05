package com.xyoye.player.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by xyoye on 2020/11/13.
 */


fun getShotImageName(): String {
    val currentTimeFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val curTime: String = currentTimeFormat.format(Date())
    return "/SHOT_$curTime.jpg"
}

fun formatDuration(time: Long): String {
    val totalSeconds = (time / 1000).toInt()
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60
    return if (minutes > 99)
        String.format(
            "%d:%02d", minutes, seconds
        ) else
        String.format("%02d:%02d", minutes, seconds)
}