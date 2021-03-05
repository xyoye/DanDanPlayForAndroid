package com.xyoye.player.utils

/**
 * Created by xyoye on 2020/11/4.
 */

interface ProgressObserver {
    fun saveProgress(url: String, position: Long, duration: Long)
}