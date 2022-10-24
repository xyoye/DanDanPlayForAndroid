package com.xyoye.player.kernel.inter

import com.xyoye.subtitle.MixedSubtitle

/**
 * Created by xyoye on 2020/10/29.
 */

interface VideoPlayerEventListener {
    fun onPrepared()

    fun onError(e: Exception? = null)

    fun onCompletion()

    fun onVideoSizeChange(width: Int, height: Int)

    fun onInfo(what: Int, extra: Int)

    fun onSubtitleTextOutput(subtitle: MixedSubtitle)
}