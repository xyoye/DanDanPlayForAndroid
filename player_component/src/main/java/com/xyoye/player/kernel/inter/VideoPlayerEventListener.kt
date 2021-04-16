package com.xyoye.player.kernel.inter

import com.xyoye.data_component.bean.VideoTrackBean
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

    fun updateTrack(isAudio: Boolean, trackData : MutableList<VideoTrackBean>)
}