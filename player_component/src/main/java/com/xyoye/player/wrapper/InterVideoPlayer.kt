package com.xyoye.player.wrapper

import android.graphics.Point
import android.graphics.PointF
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.data_component.bean.VideoStreamBean
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.surface.InterSurfaceView

/**
 * Created by xyoye on 2020/11/1.
 */

interface InterVideoPlayer {

    /**
     * 开始
     */
    fun start()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 获取当前视频资源
     */
    fun getVideoSource(): BaseVideoSource

    /**
     * 获取资源时长
     */
    fun getDuration(): Long

    /**
     * 获取当前播放进度
     */
    fun getCurrentPosition(): Long

    /**
     * 跳转至指定时间
     */
    fun seekTo(timeMs: Long)

    /**
     * 是否正在播放中
     */
    fun isPlaying(): Boolean

    /**
     * 获取缓冲进度
     */
    fun getBufferedPercentage(): Int

    /**
     * 静音
     */
    fun setSilence(isSilence: Boolean)

    /**
     * 当前是否已静音
     */
    fun isSilence(): Boolean

    /**
     * 设置音量大小
     */
    fun setVolume(point: PointF)

    /**
     * 获取当前音量
     */
    fun getVolume(): PointF

    /**
     * 设置视频缩放模式
     */
    fun setScreenScale(scaleType: VideoScreenScale)

    /**
     * 设置倍速
     */
    fun setSpeed(speed: Float)

    /**
     * 获取当前倍速
     */
    fun getSpeed(): Float

    /**
     * 获取网络加载速度
     */
    fun getTcpSpeed(): Long

    /**
     * 获取渲染布局
     */
    fun getRenderView(): InterSurfaceView?

    /**
     *  获取视频宽高
     */
    fun getVideoSize(): Point

    /**
     *  设置视频角度
     */
    fun setRotation(rotation: Float)

    /**
     *  是否由播放器处理外挂弹幕
     */
    fun interceptSubtitle(subtitlePath: String): Boolean

    /**
     * 更新字幕偏移时间
     */
    fun updateSubtitleOffsetTime()

    /**
     * 获取音轨
     */
    fun getAudioStream(): List<VideoStreamBean>

    /**
     * 获取字幕轨道
     */
    fun getSubtitleStream(): List<VideoStreamBean>

    /**
     * 选择音/字幕轨
     */
    fun selectStream(stream: VideoStreamBean)
}