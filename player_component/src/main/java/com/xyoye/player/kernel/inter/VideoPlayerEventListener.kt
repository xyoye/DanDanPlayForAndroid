package com.xyoye.player.kernel.inter

import com.xyoye.data_component.enums.PlayState
import com.xyoye.subtitle.MixedSubtitle

/**
 * Created by xyoye on 2020/10/29.
 */

interface VideoPlayerEventListener {
    fun onPrepared()

    fun onError(e: Exception? = null)

    fun onCompletion()

    fun onVideoSizeChange(width: Int, height: Int)

    /**
     * 播放内核恢复播放位置后同步控制层进度。
     *
     * @param positionMs 恢复后的播放位置，单位为毫秒
     */
    fun onPositionRestored(positionMs: Long)

    /**
     * 播放内核状态变化后同步控制层播放状态。
     *
     * @param playState 播放器控制层状态
     */
    fun onPlayStateChanged(playState: PlayState)

    fun onInfo(what: Int, extra: Int)

    fun onSubtitleTextOutput(subtitle: MixedSubtitle)
}
