package com.xyoye.player.controller.impl

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
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.controller.interfaces.*
import com.xyoye.player.controller.wrapper.ControlWrapper
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.utils.OrientationHelper

/**
 * Created by xyoye on 2020/11/2.
 */

abstract class BaseVideoController(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), InterVideoController, InterViewController,
    OnOrientationChangeListener {

    protected lateinit var mControlWrapper: ControlWrapper

    protected var mIsLocked = false
    protected var mIsShowing = false
    protected var mDefaultTimeOutMs = 5000L
    protected val mControlComponents = LinkedHashMap<InterControllerView, Boolean>()
    protected val mOrientationHelper = OrientationHelper(context)
    protected val attachLifecycle = (context as LifecycleOwner)

    //播放失败回调
    protected var mPlayErrorBlock: (() -> Unit)? = null

    //播放退出回调
    protected var mPlayExitBlock: (() -> Unit)? = null

    //选择字幕回调
    protected var mSubtitleBlock: (() -> Unit)? = null

    //选择弹幕回调
    protected var mDanmuBlock: (() -> Unit)? = null

    //隐藏视图Runnable
    protected val mFadeOut = Runnable { hideController() }

    //刷新进度Runnable
    protected var mUpdateProgress: Runnable = object : Runnable {
        override fun run() {
            val position = mControlWrapper.getCurrentPosition()
            handleProgressChanged(mControlWrapper.getDuration(), position)
            if (mControlWrapper.isPlaying()) {
                postDelayed(this, ((1000 - position % 1000) / mControlWrapper.getSpeed()).toLong())
            } else {
                mIsStartProgress = false
            }
        }
    }

    private var mIsStartProgress = false

    @CallSuper
    fun setMediaPlayer(mediaPlayer: InterVideoPlayer) {
        mControlWrapper = ControlWrapper(mediaPlayer, this)
        for (entry in mControlComponents.entries) {
            entry.key.attach(mControlWrapper)
        }
        mOrientationHelper.mOnOrientationChangeListener = this
        mOrientationHelper.enable()
    }

    override fun addControlComponent(
        vararg controllerViews: InterControllerView,
        isIndependent: Boolean
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

    override fun removeControlComponent(controllerView: InterControllerView) {
        removeView(controllerView.getView())
        mControlComponents.remove(controllerView)
    }

    override fun removeAllControlComponent() {
        for (entry in mControlComponents.entries) {
            removeView(entry.key.getView())
        }
        mControlComponents.clear()
    }

    override fun removeAllIndependentComponents() {
        val iterator = mControlComponents.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value) {
                iterator.remove()
            }
        }
    }

    override fun showSettingView(viewType: SettingViewType) {
        hideController()
        if (!mIsLocked) {
            for (entry in mControlComponents.entries) {
                val view = entry.key
                if (view is InterSettingView
                    && view.getSettingViewType() == viewType
                    && !view.isSettingShowing()
                ) {
                    view.onSettingVisibilityChanged(true)
                }
            }
        }
    }

    override fun hideSettingView() {
        for (entry in mControlComponents.entries) {
            val view = entry.key
            if (view is InterSettingView && view.isSettingShowing()) {
                view.onSettingVisibilityChanged(false)
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

    override fun showController() {
        if (!mIsShowing) {
            mIsShowing = true
            startFadeOut()
            handleVisibilityChanged(true)
        }
    }

    override fun isControllerShowing() = mIsShowing

    override fun isSettingViewShowing(): Boolean {
        for (entry in mControlComponents.entries) {
            val view = entry.key
            if (view is InterSettingView && view.isSettingShowing()) {
                return true
            }
        }
        return false
    }

    override fun startFadeOut() {
        stopFadeOut()
        postDelayed(mFadeOut, mDefaultTimeOutMs)
    }

    override fun stopFadeOut() {
        removeCallbacks(mFadeOut)
    }

    override fun setLocked(locked: Boolean) {
        mIsLocked = locked
        handleLockStateChanged(locked)
    }

    override fun isLocked() = mIsLocked

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

    open fun onProgressChanged(duration: Long, position: Long) {

    }

    open fun onVideoSizeChanged(videoSize: Point) {

    }

    open fun isWrapperInitialized() = ::mControlWrapper.isInitialized

    @CallSuper
    protected fun onPlayStateChanged(playState: PlayState) {
        when (playState) {
            PlayState.STATE_IDLE -> {
                mIsLocked = false
                mIsShowing = false
                removeAllControlComponent()
            }
            PlayState.STATE_ERROR -> {
                mIsShowing = false
                mPlayErrorBlock?.invoke()
            }
            else -> {
            }
        }
    }

    private fun handleVisibilityChanged(isVisible: Boolean) {
        if (!mIsLocked) {
            for (entry in mControlComponents.entries) {
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

    private fun handleProgressChanged(duration: Long, position: Long) {
        for (entry in mControlComponents.entries) {
            entry.key.onProgressChanged(duration, position)
        }
        onProgressChanged(duration, position)
    }
}