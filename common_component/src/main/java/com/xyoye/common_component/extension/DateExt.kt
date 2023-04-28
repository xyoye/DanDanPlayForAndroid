package com.xyoye.common_component.extension

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by xyoye on 2023/1/11
 */

fun Date?.toText(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    this ?: return ""
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}