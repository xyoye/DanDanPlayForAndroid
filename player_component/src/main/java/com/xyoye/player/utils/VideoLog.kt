package com.xyoye.player.utils

import android.util.Log
import com.xyoye.player.info.PlayerInitializer

/**
 * Created by xyoye on 2020/10/29.
 */

object VideoLog {
    const val TAG = "DanDanPlay.VideoPlayer"

    fun e(message: String?) {
        log(Log.ERROR, message)
    }

    fun d(message: String?) {
        log(Log.DEBUG, message)
    }

    fun i(message: String?) {
        log(Log.INFO, message)
    }

    private fun log(logLevel: Int, message: String?) {
        if (PlayerInitializer.isPrintLog) {
            when (logLevel) {
                Log.ERROR -> Log.e(TAG, message ?: "")
                Log.DEBUG -> Log.d(TAG, message ?: "")
                Log.INFO -> Log.i(TAG, message ?: "")
            }
        }
    }
}