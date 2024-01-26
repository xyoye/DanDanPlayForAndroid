package com.xyoye.player.controller.base

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.util.AttributeSet
import android.view.OrientationEventListener
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.TrackType
import com.xyoye.player.controller.video.InterControllerView
import com.xyoye.player.controller.video.PlayerControlView
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.utils.OrientationHelper
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player.wrapper.InterDanmuController
import com.xyoye.player.wrapper.InterSettingController
import com.xyoye.player.wrapper.InterSubtitleController
import com.xyoye.player.wrapper.InterVideoController
import com.xyoye.player.wrapper.InterVideoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by xyoye on 2020/11/2.
 */

abstract class BaseVideoController(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), InterVideoController,
    OrientationHelper.OnOrientationChangeListener {

    protected lateinit var mControlWrapper: ControlWrapper

    protected var mIsLocked = false
    protected var mIsShowing = false
    protected var mIsPopupMode = false
    protected var mDefaultTimeOutMs = 5000L
    protected val mControlComponents = LinkedHashMap<InterControllerView, Boolean>()
    protected val mOrientationHelper = OrientationHelper(context)
    protected val attachLifecycle = (context as LifecycleOwner)

    //播放失败回调
    protected var mPlayErrorBlock: (() -> Unit)? = null

    //播放完成回调
    protected var mPlayCompletionBlock: (() -> Unit)? = null

    //隐藏视图Runnable
    protected var mHideControllerJob: Job? = null

    //刷新进度Runnable
    protected var mUpdateProgress: Runnable = object : Runnable {
        override fun run() {
            val position = mControlWrapper.getCurrentPosition()
            handleProgressChanged(mControlWrapper.getDuration(), position)
            postDelayed(this, 500L)
        }
    }

    private var mIsStartProgress = false

    @CallSuper
    fun setMediaPlayer(mediaPlayer: InterVideoPlayer) {
        val videoController = this
        val danmuController = getDanmuController()
        val subtitleController = getSubtitleController()
        val settingController = getSettingController()

        mControlWrapper = ControlWrapper(
            mediaPlayer,
            videoController,
            danmuController,
            subtitleController,
            settingController
        )
        for (entry in mControlComponents.entries) {
            entry.key.attach(mControlWrapper)
        }
        mOrientationHelper.mOnOrientationChangeListener = this
        mOrientationHelper.enable()
    }

    protected fun addControlComponent(
        vararg controllerViews: InterControllerView,
        isIndependent: Boolean = false
    ) {
        for (controllerView in controllerViews) {
            mControlComponents[controllerView] = isIndependent
            if (this::mControlWrapper.isInitialized) {
                controllerView.attach(mControlWrapper)
            }
            if (!isIndependent) {
                addView(controllerView.getView())
            }
        }
    }

    protected fun removeControlComponent(controllerView: InterControllerView) {
        removeView(controllerView.getView())
        mControlComponents.remove(controllerView)
    }

    override fun setTrackUpdated(type: TrackType) {
        for (entry in mControlComponents.entries) {
            entry.key.onTrackChanged(type)
        }
    }

    protected fun removeAllControlComponent() {
        for (entry in mControlComponents.entries) {
            removeView(entry.key.getView())
        }
        mControlComponents.clear()
    }

    protected fun removeAllIndependentComponents() {
        val iterator = mControlComponents.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value) {
                iterator.remove()
            }
        }
    }

    override fun hideController() {
        if (mIsShowing) {
            mIsShowing = false
            stopFadeOut()
            handleVisibilityChanged(false)
        }
    }

    override fun showController(ignoreShowing: Boolean) {
        if (ignoreShowing || !mIsShowing) {
            mIsShowing = true
            startFadeOut()
            handleVisibilityChanged(true)
        }
    }

    override fun isControllerShowing() = mIsShowing

    override fun startFadeOut() {
        mHideControllerJob?.cancel()
        mHideControllerJob = attachLifecycle.lifecycleScope.launch {
            delay(mDefaultTimeOutMs)
            withContext(Dispatchers.Main) {
                hideController()
            }
        }
    }

    override fun stopFadeOut() {
        mHideControllerJob?.cancel()
    }

    override fun setLocked(locked: Boolean) {
        mIsLocked = locked
        handleLockStateChanged(locked)
    }

    override fun isLocked() = mIsLocked

    override fun setPopupMode(isPopup: Boolean) {
        mIsPopupMode = isPopup
        handlePopupModeChanged(isPopup)
    }

    override fun isPopupMode() = mIsPopupMode

    override fun startProgress() {
        if (mIsStartProgress)
            return
        mIsStartProgress = true
        post(mUpdateProgress)
    }

    override fun stopProgress() {
        if (!mIsStartProgress)
            return
        mIsStartProgress = false
        removeCallbacks(mUpdateProgress)
    }

    override fun setProgress(position: Long) {
        handleProgressChanged(mControlWrapper.getDuration(), position)
    }

    override fun onOrientationChanged(orientation: Int) {
        if (!PlayerInitializer.isOrientationEnabled)
            return

        if (attachLifecycle.lifecycle.currentState == Lifecycle.State.DESTROYED)
            return

        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN)
            return

        val attachActivity = (context as AppCompatActivity)
        if (orientation in 60..120) {
            attachActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        } else if (orientation in 240..300) {
            attachActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    @CallSuper
    open fun setPlayState(playState: PlayState) {
        if (playState == PlayState.STATE_IDLE || playState == PlayState.STATE_START_ABORT) {
            mOrientationHelper.disable()
        } else {
            mOrientationHelper.enable()
        }

        for (entry in mControlComponents.entries) {
            entry.key.onPlayStateChanged(playState)
        }
        onPlayStateChanged(playState)
    }

    @CallSuper
    open fun setVideoSize(videoSize: Point) {
        for (entry in mControlComponents.entries) {
            entry.key.onVideoSizeChanged(videoSize)
        }
        onVideoSizeChanged(videoSize)
    }

    open fun setDismissTimeOut(timeOut: Long) {
        mDefaultTimeOutMs = if (timeOut > 0) {
            timeOut
        } else {
            5000L
        }
    }

    open fun onBackPressed() = false

    open fun togglePlay() {
        mControlWrapper.togglePlay()
    }


    open fun onVisibilityChanged(isVisible: Boolean) {

    }

    open fun onLockStateChanged(isLocked: Boolean) {

    }

    open fun onPopupModeChanged(isPopup: Boolean) {

    }

    open fun onProgressChanged(duration: Long, position: Long) {

    }

    open fun onVideoSizeChanged(videoSize: Point) {

    }

    open fun isWrapperInitialized() = ::mControlWrapper.isInitialized

    open fun release() {
        getDanmuController().danmuRelease()
        getSettingController().settingRelease()
    }

    @CallSuper
    open fun onPlayStateChanged(playState: PlayState) {
        when (playState) {
            PlayState.STATE_ERROR -> {
                mPlayErrorBlock?.invoke()
            }

            else -> {
            }
        }
    }

    private fun handleVisibilityChanged(isVisible: Boolean) {
        for (entry in mControlComponents.entries) {
            if (entry.key is PlayerControlView) {
                entry.key.onVisibilityChanged(isVisible)
            } else if (mIsLocked.not()) {
                entry.key.onVisibilityChanged(isVisible)
            }
        }
        onVisibilityChanged(isVisible)
    }

    private fun handleLockStateChanged(isLocked: Boolean) {
        for (entry in mControlComponents.entries) {
            entry.key.onLockStateChanged(isLocked)
        }
        onLockStateChanged(isLocked)
    }

    private fun handlePopupModeChanged(isPopup: Boolean) {
        for (entry in mControlComponents.entries) {
            entry.key.onPopupModeChanged(isPopup)
        }
        onPopupModeChanged(isPopup)
    }

    private fun handleProgressChanged(duration: Long, position: Long) {
        for (entry in mControlComponents.entries) {
            entry.key.onProgressChanged(duration, position)
        }
        onProgressChanged(duration, position)
    }

    abstract fun getDanmuController(): InterDanmuController

    abstract fun getSubtitleController(): InterSubtitleController

    abstract fun getSettingController(): InterSettingController
}