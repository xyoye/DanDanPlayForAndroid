package com.xyoye.player.controller.interfaces

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.enums.VideoScreenScale

/**
 * Created by xyoye on 2020/11/1.
 */

interface InterVideoPlayer {

    fun start()

    fun pause()

    fun getDuration(): Long

    fun getCurrentPosition(): Long

    fun seekTo(timeMs: Long)

    fun isPlaying(): Boolean

    fun getBufferedPercentage(): Int

    fun setSilence(isSilence: Boolean)

    fun isSilence(): Boolean

    fun setVolume(point: PointF)

    fun getVolume(): PointF

    fun setScreenScale(scaleType: VideoScreenScale)

    fun setSpeed(speed: Float)

    fun getSpeed(): Float

    fun getTcpSpeed(): Long

    fun setMirrorRotation(enable: Boolean)

    fun doScreenShot(): Bitmap?

    fun getVideoSize(): Point

    fun setRotation(rotation: Float)

    fun selectTrack(select: VideoTrackBean?, deselect: VideoTrackBean?)
}