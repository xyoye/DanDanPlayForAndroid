package com.xyoye.player

import android.content.Context
import android.graphics.PointF
import android.media.AudioManager
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xyoye.cache.CacheManager
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.data_component.bean.VideoStreamBean
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.controller.VideoController
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.kernel.facoty.PlayerFactory
import com.xyoye.player.kernel.inter.AbstractVideoPlayer
import com.xyoye.player.kernel.inter.VideoPlayerEventListener
import com.xyoye.player.surface.InterSurfaceView
import com.xyoye.player.surface.SurfaceFactory
import com.xyoye.player.utils.AudioFocusHelper
import com.xyoye.player.utils.PlayerConstant
import com.xyoye.player.wrapper.InterVideoPlayer
import com.xyoye.player_component.utils.PlayRecorder
import com.xyoye.subtitle.MixedSubtitle

/**
 * Created by xyoye on 2020/11/3.
 */

class DanDanVideoPlayer(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs, 0), InterVideoPlayer, VideoPlayerEventListener {
    //播放状态
    private var mCurrentPlayState = PlayState.STATE_IDLE

    //默认组件参数
    private val mDefaultLayoutParams = LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
        Gravity.CENTER
    )

    //音频焦点监听
    private var mAudioFocusHelper: AudioFocusHelper

    //视图控制器
    private var mVideoController: VideoController? = null

    //渲染视图组件
    private var mRenderView: InterSurfaceView? = null

    //播放器
    private lateinit var mVideoPlayer: AbstractVideoPlayer

    //播放资源
    private lateinit var videoSource: BaseVideoSource

    //当前音量
    private var mCurrentVolume = PointF(0f, 0f)

    //当前视图缩放类型
    private var mScreenScale = PlayerInitializer.screenScale

    init {
        val audioManager = context.applicationContext
            .getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val lifecycleScope = (context as AppCompatActivity).lifecycleScope
        mAudioFocusHelper = AudioFocusHelper(this, audioManager, lifecycleScope)
    }

    override fun start() {
        if (mVideoController == null) {
            throw RuntimeException("controller must initialized before start")
        }

        var isStartedPlay = false
        if (mCurrentPlayState == PlayState.STATE_IDLE || mCurrentPlayState == PlayState.STATE_START_ABORT) {
            initPlayer()
            isStartedPlay = startPrepare()
        } else if (isInPlayState()) {
            mVideoPlayer.start()
            setPlayState(PlayState.STATE_PLAYING)
            isStartedPlay = true
        }

        if (isStartedPlay) {
            keepScreenOn = true
            mAudioFocusHelper.requestFocus()
        }
    }

    override fun pause() {
        if (isInPlayState() && mVideoPlayer.isPlaying()) {
            setPlayState(PlayState.STATE_PAUSED)
            mVideoPlayer.pause()
            mAudioFocusHelper.abandonFocus()
            keepScreenOn = false
        }
    }

    override fun getVideoSource(): BaseVideoSource {
        return videoSource
    }

    override fun getDuration(): Long {
        if (isInPlayState())
            return mVideoPlayer.getDuration()
        return 0
    }

    override fun getCurrentPosition(): Long {
        if (isInPlayState())
            return mVideoPlayer.getCurrentPosition()
        return 0
    }

    override fun seekTo(timeMs: Long) {
        if (timeMs >= 0 && isInPlayState()) {
            mVideoPlayer.seekTo(timeMs)
        }
    }

    override fun isPlaying() = isInPlayState() && mVideoPlayer.isPlaying()

    override fun getBufferedPercentage() = mVideoPlayer.getBufferedPercentage()

    override fun setSilence(isSilence: Boolean) {
        val volume = if (isSilence) 0f else 1f
        setVolume(PointF(volume, volume))
    }

    override fun isSilence(): Boolean {
        return mCurrentVolume.x + mCurrentVolume.y == 0f
    }

    override fun setVolume(point: PointF) {
        mCurrentVolume = point
        mVideoPlayer.setVolume(mCurrentVolume.x, mCurrentVolume.y)
    }

    override fun getVolume() = mCurrentVolume

    override fun setScreenScale(scaleType: VideoScreenScale) {
        mScreenScale = scaleType
        mRenderView?.setScaleType(mScreenScale)
    }

    override fun setSpeed(speed: Float) {
        if (isInPlayState()) {
            mVideoPlayer.setSpeed(speed)
        }
    }

    override fun getSpeed(): Float {
        if (isInPlayState()) {
            return mVideoPlayer.getSpeed()
        }
        return 1f
    }

    override fun getTcpSpeed() = mVideoPlayer.getTcpSpeed()

    override fun getRenderView(): InterSurfaceView? {
        return mRenderView
    }

    override fun getVideoSize() = mVideoPlayer.getVideoSize()

    override fun interceptSubtitle(subtitlePath: String): Boolean {
        return mVideoPlayer.interceptSubtitle(subtitlePath)
    }

    override fun addAudioStream(audioPath: String) {
        mVideoPlayer.addAudioStream(audioPath)
    }

    override fun onVideoSizeChange(width: Int, height: Int) {
        mRenderView?.setScaleType(mScreenScale)
        mRenderView?.setVideoSize(width, height)
        mVideoController?.setVideoSize(mVideoPlayer.getVideoSize())
    }

    override fun onPrepared() {
        setPlayState(PlayState.STATE_PREPARED)
    }

    override fun onError(e: Exception?) {
        setPlayState(PlayState.STATE_ERROR)
        keepScreenOn = false
    }

    override fun onCompletion() {
        if (mCurrentPlayState != PlayState.STATE_ERROR) {
            setPlayState(PlayState.STATE_COMPLETED)
        }
        keepScreenOn = false
        PlayRecorder.recordProgress(videoSource, 0, getDuration())
    }

    override fun onInfo(what: Int, extra: Int) {
        when (what) {
            PlayerConstant.MEDIA_INFO_BUFFERING_START -> {
                setPlayState(PlayState.STATE_BUFFERING_PAUSED)
            }

            PlayerConstant.MEDIA_INFO_BUFFERING_END -> {
                setPlayState(PlayState.STATE_BUFFERING_PLAYING)
            }

            PlayerConstant.MEDIA_INFO_VIDEO_RENDERING_START -> {
                setPlayState(PlayState.STATE_PLAYING)
                if (windowVisibility != View.VISIBLE) {
                    pause()
                }
            }

            PlayerConstant.MEDIA_INFO_VIDEO_ROTATION_CHANGED -> {
                mRenderView?.setVideoRotation(extra)
            }

            PlayerConstant.MEDIA_INFO_URL_EMPTY -> {
                setPlayState(PlayState.STATE_ERROR)
            }
        }
    }

    override fun onSubtitleTextOutput(subtitle: MixedSubtitle) {
        mVideoController?.onSubtitleTextOutput(subtitle)
    }

    private fun initPlayer() {
        mAudioFocusHelper.enable = PlayerInitializer.isEnableAudioFocus
        //初始化播放器
        mVideoPlayer = PlayerFactory.getFactory(PlayerInitializer.playerType)
            .createPlayer(context).apply {
                setPlayerEventListener(this@DanDanVideoPlayer)
                initPlayer()
            }

        //初始化渲染布局
        mRenderView?.apply {
            this@DanDanVideoPlayer.removeView(getView())
            release()
        }
        mRenderView = SurfaceFactory.getFactory(
            PlayerInitializer.playerType, PlayerInitializer.surfaceType
        ).createRenderView(context)
            .apply {
                this@DanDanVideoPlayer.addView(getView(), 0, mDefaultLayoutParams)
                attachPlayer(mVideoPlayer)
            }

        setExtraOption()
    }

    private fun setExtraOption() {
        mVideoPlayer.setLooping(PlayerInitializer.isLooping)
    }

    private fun startPrepare(): Boolean {
        return if (videoSource.getVideoUrl().isNotEmpty()) {
            mVideoPlayer.setDataSource(videoSource.getVideoUrl(), videoSource.getHttpHeader())
            mVideoPlayer.prepareAsync()
            setPlayState(PlayState.STATE_PREPARING)
            true
        } else {
            setPlayState(PlayState.STATE_ERROR)
            false
        }
    }

    private fun setPlayState(playState: PlayState) {
        mCurrentPlayState = playState
        mVideoController?.setPlayState(playState)
    }

    private fun isInPlayState(): Boolean {
        return this::mVideoPlayer.isInitialized
                && mCurrentPlayState != PlayState.STATE_ERROR
                && mCurrentPlayState != PlayState.STATE_IDLE
                && mCurrentPlayState != PlayState.STATE_PREPARING
                && mCurrentPlayState != PlayState.STATE_START_ABORT
                && mCurrentPlayState != PlayState.STATE_COMPLETED
    }

    fun resume() {
        if (isInPlayState() && !mVideoPlayer.isPlaying()) {
            setPlayState(PlayState.STATE_PLAYING)
            mVideoPlayer.start()
            mAudioFocusHelper.requestFocus()
            keepScreenOn = true
        }
    }

    /**
     * 保存播放信息
     */
    fun recordPlayInfo() {
        if (this::videoSource.isInitialized.not()) {
            return
        }
        //保存最后一帧
        PlayRecorder.recordImage(videoSource.getUniqueKey(), mRenderView)
        //保存播放进度
        PlayRecorder.recordProgress(videoSource, getCurrentPosition(), getDuration())
    }

    fun release() {
        if (mCurrentPlayState != PlayState.STATE_IDLE) {
            //释放缓存
            CacheManager.release()
            //释放播放器控制器
            mVideoController?.destroy()
            //释放播放器
            mVideoPlayer.release()
            //关闭常亮
            keepScreenOn = false
            //释放渲染布局
            mRenderView?.run {
                this@DanDanVideoPlayer.removeView(getView())
                release()
            }
            //取消音频焦点
            mAudioFocusHelper.abandonFocus()
            //重置播放状态
            setPlayState(PlayState.STATE_IDLE)
        }
    }

    fun onBackPressed(): Boolean {
        return mVideoController?.onBackPressed() ?: false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                mVideoController?.onVolumeKeyDown(true)
                return true
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                mVideoController?.onVolumeKeyDown(false)
                return true
            }

            else -> mVideoController?.onKeyDown(keyCode, event) ?: false
        }
    }

    fun setVideoSource(source: BaseVideoSource) {
        videoSource = source
    }

    fun setController(controller: VideoController?) {
        removeView(mVideoController)
        mVideoController = controller
        mVideoController?.let {
            it.setMediaPlayer(this)
            addView(it, mDefaultLayoutParams)
        }
    }

    fun enterPopupMode() {
        mVideoController?.setPopupMode(true)
        mRenderView?.refresh()
    }

    fun exitPopupMode() {
        mVideoController?.setPopupMode(false)
        mRenderView?.refresh()
    }

    fun setPopupGestureHandler(handler: OnTouchListener?) {
        mVideoController?.setPopupGestureHandler(handler)
    }

    override fun updateSubtitleOffsetTime() {
        mVideoPlayer.setSubtitleOffset(PlayerInitializer.Subtitle.offsetPosition)
    }

    override fun getAudioStream(): List<VideoStreamBean> {
        return mVideoPlayer.getAudioStream()
    }

    override fun getSubtitleStream(): List<VideoStreamBean> {
        return mVideoPlayer.getSubtitleStream()
    }

    override fun selectStream(stream: VideoStreamBean) {
        return mVideoPlayer.selectStream(stream)
    }
}