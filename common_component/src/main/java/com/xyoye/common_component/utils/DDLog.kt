package com.xyoye.common_component.utils

import android.util.Log
import java.util.logging.Level

/**
 * Created by xyoye on 2020/12/29.
 */

object DDLog {
    var enable = true

    fun i(message: String, throwable: Throwable? = null) {
        i(null, message, throwable)
    }

    fun i(tag: String?, message: String, throwable: Throwable? = null) {
        log(tag, message, Level.INFO, throwable)
    }

    fun w(message: String, throwable: Throwable? = null) {
        w(null, message, throwable)
    }

    fun w(tag: String?, message: String, throwable: Throwable? = null) {
        log(tag, message, Level.WARNING, throwable)
    }

    fun e(message: String, throwable: Throwable? = null) {
        e(null, message, throwable)
    }

    fun e(tag: String?, message: String, throwable: Throwable? = null) {
        log(tag, message, Level.SEVERE, throwable)
    }

    private fun log(tag: String?, message: String, level: Level, throwable: Throwable? = null) {
        if (!enable)
            return

        val logTag = DDLog::class.java.simpleName

        val logMsg = if (tag.isNullOrEmpty()) message else "$tag $message"

        when (level) {
            Level.INFO -> Log.i(logTag, logMsg, throwable)
            Level.WARNING -> Log.w(logTag, logMsg, throwable)
            Level.SEVERE -> Log.e(logTag, logMsg, throwable)
        }
    }
}