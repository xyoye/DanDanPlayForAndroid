package com.xyoye.player.kernel.inter

import android.view.Surface
import com.xyoye.data_component.bean.VideoStreamBean

/**
 * Created by xyoye on 2020/10/29.
 */

abstract class AbstractVideoPlayer {

    //播放器事件回调
    protected lateinit var mPlayerEventListener: VideoPlayerEventListener

    /**
     * 设置播放器回调
     */
    fun setPlayerEventListener(playerEventListener: VideoPlayerEventListener) {
        mPlayerEventListener = playerEventListener
    }

    /**
     *  初始化播放器
     */
    abstract fun initPlayer()

    /**
     * 播放器参数配置
     */
    abstract fun setOptions()

    /**
     *  设置播放资源
     */
    abstract fun setDataSource(path: String, headers: Map<String, String>? = null)

    /**
     *  设置视频载体
     */
    abstract fun setSurface(surface: Surface)

    /**
     *  异步准备播放
     */
    abstract fun prepareAsync()

    /*-----------控制播放器-------------------*/

    /**
     * 播饭
     */
    abstract fun start()

    /**
     * 暂停
     */
    abstract fun pause()

    /**
     * 停止
     */
    abstract fun stop()

    /**
     * 重置
     */
    abstract fun reset()

    /**
     * 释放
     */
    abstract fun release()

    /**
     * 跳转至具体位置
     */
    abstract fun seekTo(timeMs: Long)

    /**
     * 设置视频倍速
     */
    abstract fun setSpeed(speed: Float)

    /**
     * 设置音量大小
     */
    abstract fun setVolume(leftVolume: Float, rightVolume: Float)

    /**
     * 视频是否循环播放
     */
    abstract fun setLooping(isLooping: Boolean)

    /**
     * 字幕时间调节
     */
    abstract fun setSubtitleOffset(offsetMs: Long)

    /*------------播放器状态-------------------*/

    /**
     * 是否正在播放
     */
    abstract fun isPlaying(): Boolean

    /**
     * 获取当前进度
     */
    abstract fun getCurrentPosition(): Long

    /**
     * 获取视频时长
     */
    abstract fun getDuration(): Long

    /**
     * 获取当前倍速
     */
    abstract fun getSpeed(): Float

    /**
     * 获取缓冲进度
     */
    abstract fun getBufferedPercentage(): Int

    /**
     * 获取网络加载速度
     */
    abstract fun getTcpSpeed(): Long

    /**
     * 获取音轨
     */
    abstract fun getAudioStream(): List<VideoStreamBean>

    /**
     * 获取字幕轨
     */
    abstract fun getSubtitleStream(): List<VideoStreamBean>

    /**
     * 选择音/字幕轨
     */
    abstract fun selectStream(stream: VideoStreamBean)

    /**
     * 是否由播放器处理外挂弹幕
     */
    open fun interceptSubtitle(subtitlePath: String) = false
}