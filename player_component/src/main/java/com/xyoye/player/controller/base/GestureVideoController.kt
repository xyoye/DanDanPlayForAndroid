package com.xyoye.player.controller.base

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.xyoye.common_component.utils.getScreenWidth
import com.xyoye.common_component.utils.isScreenEdge
import com.xyoye.data_component.enums.PlayState
import com.xyoye.player.controller.video.InterGestureView
import com.xyoye.player.utils.LongPressAccelerator
import com.xyoye.player.utils.VideoZoomHelper
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Created by xyoye on 2020/11/2.
 */

abstract class GestureVideoController(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TvVideoController(context, attrs, defStyleAttr), View.OnTouchListener,
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {

    private val mAudioManager: AudioManager by lazy {
        getContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val mGestureDetector: GestureDetector by lazy {
        GestureDetector(getContext(), this)
    }

    /**
     * 画面缩放助手
     *
     * 目标视图为当前渲染视图（RenderSurfaceView / RenderTextureView）
     */
    private val mZoomHelper = VideoZoomHelper {
        if (isWrapperInitialized()) mControlWrapper.getRenderView()?.getView() else null
    }

    /**
     * 双指缩放手势检测
     */
    private val mScaleGestureDetector: ScaleGestureDetector by lazy {
        ScaleGestureDetector(
            getContext(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    // focusX / focusY 相对本控制器，加上 left / top 换算为父容器坐标
                    mZoomHelper.zoomBy(
                        detector.scaleFactor,
                        detector.focusX + left,
                        detector.focusY + top
                    )
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    mZoomHelper.settle()
                }
            }
        ).apply {
            // 双击已用于播放/暂停与缩放复位，关闭「双击拖动」快速缩放避免冲突
            isQuickScaleEnabled = false
        }
    }

    private var mPopupGestureHandler: OnTouchListener? = null

    private var mCurrentPlayState = PlayState.STATE_IDLE

    private var mStreamVolume = 0
    private var mBrightness = 0f
    private var mSeekPosition = -1L

    private var mFirstTouch = false
    private var mChangePosition = false
    private var mChangeBrightness = false
    private var mChangeVolume = false

    private val longPressAccelerator: LongPressAccelerator by lazy {
        LongPressAccelerator(
            mControlWrapper,
            onStart = { speed -> startAccelerate(speed) },
            onStop = { stopAccelerate() }
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOnTouchListener(this)
    }

    override fun setPlayState(playState: PlayState) {
        super.setPlayState(playState)
        mCurrentPlayState = playState
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        mPopupGestureHandler?.onTouch(v, event)

        // 悬浮窗模式下不响应缩放手势
        if (isPopupMode().not() && mZoomHelper.canZoom()) {
            mScaleGestureDetector.onTouchEvent(event)
        }

        // 多指操作期间仅处理缩放，不再下发普通手势，避免误触进度/音量/亮度
        if (mScaleGestureDetector.isInProgress) {
            return true
        }

        val handled = mGestureDetector.onTouchEvent(event)

        // 手指全部抬起时处理越界回弹
        if (event.actionMasked == MotionEvent.ACTION_UP ||
            event.actionMasked == MotionEvent.ACTION_CANCEL
        ) {
            if (mZoomHelper.isZoomed()) {
                mZoomHelper.settle()
            }
        }
        return handled
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mGestureDetector.onTouchEvent(event).not() && isPopupMode().not()) {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    longPressAccelerator.disable()
                    stopSlide()
                    if (mSeekPosition >= 0) {
                        mControlWrapper.seekTo(mSeekPosition)
                        mSeekPosition = -1L
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    stopSlide()
                    mSeekPosition = -1L
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        longPressAccelerator.enable()
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onDoubleTapEvent(e: MotionEvent) = false

    override fun onSingleTapUp(e: MotionEvent) = false

    override fun onDown(e: MotionEvent): Boolean {
        if (!isNormalPlayState() or context.isScreenEdge(e)) {
            return true
        }
        mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mBrightness = (context as Activity).window.attributes.screenBrightness

        mFirstTouch = true
        mChangePosition = false
        mChangeBrightness = false
        mChangeVolume = false
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        if (isNormalPlayState()) {
            mControlWrapper.toggleVisible()
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        if (!isLocked() and isNormalPlayState()) {
            // 已放大时双击复位画面，否则保持原有的播放/暂停
            if (mZoomHelper.isZoomed()) {
                mZoomHelper.reset(true)
            } else {
                togglePlay()
            }
        }
        return true
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (isPopupMode()) {
            return false
        }
        if (mScaleGestureDetector.isInProgress) {
            return true
        }

        // 已放大：单指拖动改为平移画面
        if (mZoomHelper.isZoomed()) {
            mZoomHelper.translateBy(-distanceX, -distanceY)
            return true
        }

        if (!isNormalPlayState() or isLocked() or context.isScreenEdge(e1)) {
            return true
        }

        val eventX1 = e1.x
        val eventX2 = e2.x
        val eventY1 = e1.y
        val eventY2 = e2.y

        val deltaX = eventX1 - eventX2
        val deltaY = eventY1 - eventY2

        if (mFirstTouch) {
            //垂直滑动
            if (abs(distanceX) < abs(distanceY)) {
                val halfScreen = context.getScreenWidth() / 2
                if (eventX2 > halfScreen) {
                    //右半屏，修改音量
                    mChangeVolume = true
                } else {
                    //左半屏，修改亮度
                    mChangeBrightness = true
                }
            } else {
                //水平滑动
                mChangePosition = true
            }
            for (entry in mControlComponents.entries) {
                val view = entry.key
                if (view is InterGestureView) {
                    view.onStartSlide()
                }
            }
            mFirstTouch = false
        }

        when {
            mChangePosition -> slideToChangePosition(deltaX)
            mChangeBrightness -> slideToChangeBrightness(deltaY)
            mChangeVolume -> slideToChangeVolume(deltaY)
        }
        return true
    }

    protected fun slideToChangePosition(deltaX: Float) {
        //滑动距离与实际进度缩放比例
        val zoomPercent = 120 * 1000
        val duration = mControlWrapper.getDuration()
        val currentPosition = mControlWrapper.getCurrentPosition()

        //新位置
        var newPosition = (-deltaX / measuredWidth * zoomPercent + currentPosition).toLong()

        newPosition = max(0, newPosition)
        newPosition = min(duration, newPosition)

        for (entry in mControlComponents.entries) {
            val view = entry.key
            if (view is InterGestureView) {
                view.onPositionChange(newPosition, currentPosition, duration)
            }
        }
        mSeekPosition = newPosition
    }

    protected fun slideToChangeBrightness(deltaY: Float) {
        mBrightness = if (mBrightness == -1f) 0.5f else mBrightness

        var newBrightness = deltaY * 2f / measuredHeight * 1.0f + mBrightness

        newBrightness = max(0f, newBrightness)
        newBrightness = min(1f, newBrightness)

        val window = (context as Activity).window
        val attribute = window.attributes
        attribute.screenBrightness = newBrightness
        window.attributes = attribute

        for (entry in mControlComponents.entries) {
            val view = entry.key
            if (view is InterGestureView) {
                view.onBrightnessChange((newBrightness * 100).toInt())
            }
        }
    }

    protected fun slideToChangeVolume(deltaY: Float) {
        val maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        var newVolume = (deltaY * 2 / measuredHeight * maxVolume + mStreamVolume).toInt()

        newVolume = max(0, newVolume)
        newVolume = min(maxVolume, newVolume)

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)

        val percent = (newVolume.toFloat() / maxVolume.toFloat() * 100).toInt()
        for (entry in mControlComponents.entries) {
            val view = entry.key
            if (view is InterGestureView) {
                view.onVolumeChange(percent)
            }
        }
    }

    fun onVolumeKeyDown(isVolumeUp: Boolean) {
        val maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        var newVolume = if (isVolumeUp) {
            curVolume + (maxVolume / 15)
        } else {
            curVolume - (maxVolume / 15)
        }

        newVolume = max(0, newVolume)
        newVolume = min(maxVolume, newVolume)

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)

        val percent = (newVolume.toFloat() / maxVolume.toFloat() * 100).toInt()
        for (entry in mControlComponents.entries) {
            val view = entry.key
            if (view is InterGestureView) {
                view.onStartSlide()
                view.onVolumeChange(percent)
                view.onStopSlide()
            }
        }
    }

    fun setPopupGestureHandler(handler: OnTouchListener?) {
        mPopupGestureHandler = handler
    }

    /**
     * 复位画面缩放
     *
     * 切换画面比例、切换视频源、切换悬浮窗等情况需调用，避免缩放状态与渲染视图尺寸不一致
     *
     * @param animate 是否使用动画过渡
     */
    fun resetZoom(animate: Boolean = false) {
        mZoomHelper.reset(animate)
    }

    override fun onPopupModeChanged(isPopup: Boolean) {
        super.onPopupModeChanged(isPopup)
        mZoomHelper.reset(false)
    }

    private fun stopSlide() {
        for (entry in mControlComponents.entries) {
            val view = entry.key
            if (view is InterGestureView) {
                view.onStopSlide()
            }
        }
    }

    private fun startAccelerate(speed: Float) {
        for (entry in mControlComponents.entries) {
            val view = entry.key
            if (view is InterGestureView) {
                view.onStartAccelerate(speed)
            }
        }
    }

    private fun stopAccelerate() {
        for (entry in mControlComponents.entries) {
            val view = entry.key
            if (view is InterGestureView) {
                view.onStopAccelerate()
            }
        }
    }

    private fun isNormalPlayState() = isWrapperInitialized() and
            (mCurrentPlayState != PlayState.STATE_ERROR) and
            (mCurrentPlayState != PlayState.STATE_IDLE) and
            (mCurrentPlayState != PlayState.STATE_START_ABORT)
}