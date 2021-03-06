package com.xyoye.player.kernel.inter

import android.view.Surface
import com.xyoye.data_component.bean.VideoTrackBean

/**
 * Created by xyoye on 2020/10/29.
 */

abstract class AbstractVideoPlayer {

    protected lateinit var mPlayerEventListener: VideoPlayerEventListener

    fun setPlayerEventListener(playerEventListener: VideoPlayerEventListener) {
        mPlayerEventListener = playerEventListener
    }


    /*-----------播放器初始化----------------*/

    abstract fun initPlayer()

    abstract fun setDataSource(path: String, headers: Map<String, String>? = null)

    abstract fun setSurface(surface: Surface)

    abstract fun prepareAsync()

    /*-----------控制播放器-------------------*/

    abstract fun start()

    abstract fun pause()

    abstract fun stop()

    abstract fun reset()

    abstract fun release()

    abstract fun seekTo(timeMs: Long)

    abstract fun setSpeed(speed: Float)

    abstract fun setVolume(leftVolume: Float, rightVolume: Float)

    abstract fun setLooping(isLooping: Boolean)

    abstract fun setOptions()

    abstract fun selectTrack(select: VideoTrackBean?, deselect: VideoTrackBean?)

    /*------------播放器状态-------------------*/

    abstract fun isPlaying(): Boolean

    abstract fun getCurrentPosition(): Long

    abstract fun getDuration(): Long

    abstract fun getSpeed(): Float

    abstract fun getBufferedPercentage(): Int

    abstract fun getTcpSpeed(): Long
}