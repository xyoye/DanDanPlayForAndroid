package com.anjiu.repository.mmkv.processor.extension

import java.util.Locale

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2024/4/2
 *    desc  :
 */

/**
 * 首字母大写
 */
fun String.toUpperCaseInitials(): String {
    return this.substring(0, 1).uppercase(Locale.getDefault()) + this.substring(1)
}

/**
 * 小写下划线
 */
fun String.toLowerCaseUnderline(): String {
    val sb = StringBuilder()
    for (i in this.indices) {
        val c = this[i]
        if (c.isUpperCase()) {
            sb.append("_")
            sb.append(c.lowercaseChar())
        } else {
            sb.append(c)
        }
    }
    return sb.toString()
}