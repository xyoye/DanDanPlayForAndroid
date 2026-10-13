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
                    onZoomStateChanged()
                }
            }
        ).apply {
            // 双击已用于播放/暂停与缩放复位，关闭「双击拖动」快速缩放避免冲突
            isQuickScaleEnabled = false
        }
    }

    private var mPopupGestureHandler: OnTouchListener? = null

    /**
     * 双指平移画面的跟踪状态
     *
     * ScaleGestureDetector 只回调缩放倍数，不回调平移，
     * 因此这里自行跟踪多指重心位移来换算画面平移量。
     */
    private var mPointerTracked = false
    private var mLastPointerX = 0f
    private var mLastPointerY = 0f

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

        // 悬浮窗模式 / 锁定状态下不响应画面缩放、平移手势
        if (isPopupMode().not() && isLocked().not() && mZoomHelper.canZoom()) {
            mScaleGestureDetector.onTouchEvent(event)
            handleMultiPointerPan(event)
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
            // 手势结束后放弃多指重心基准，下次多指操作重新确立基准点
            mPointerTracked = false
            if (isLocked().not() && mZoomHelper.isZoomed()) {
                mZoomHelper.settle()
                onZoomStateChanged()
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

    /**
     * 双指平移画面
     *
     * 与 [mScaleGestureDetector] 的缩放并行工作：缩放在 onScale 中生效，
     * 平移在此处通过跟踪多指重心位移实现，因此捏合缩放与拖动画面可同时进行。
     *
     * 仅在画面已放大时才有实际位移，未放大时由 [VideoZoomHelper.translateBy] 内部忽略。
     */
    private fun handleMultiPointerPan(event: MotionEvent) {
        // 单指或手指全部抬起时结束跟踪
        if (event.pointerCount < 2) {
            mPointerTracked = false
            return
        }

        // 指针增减的瞬间重置基准点，避免重心跳变导致画面抖动
        if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN ||
            event.actionMasked == MotionEvent.ACTION_POINTER_UP
        ) {
            mPointerTracked = false
            return
        }

        var sumX = 0f
        var sumY = 0f
        for (index in 0 until event.pointerCount) {
            sumX += event.getX(index)
            sumY += event.getY(index)
        }
        val pointerX = sumX / event.pointerCount
        val pointerY = sumY / event.pointerCount

        if (!mPointerTracked) {
            mPointerTracked = true
            mLastPointerX = pointerX
            mLastPointerY = pointerY
            return
        }

        val dx = pointerX - mLastPointerX
        val dy = pointerY - mLastPointerY
        mLastPointerX = pointerX
        mLastPointerY = pointerY

        if (dx != 0f || dy != 0f) {
            mZoomHelper.translateBy(dx, dy)
        }
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
                resetZoom(true)
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

        // 双指操作（缩放 / 平移）期间不下发单指手势，避免误触进度 / 音量 / 亮度
        if (mScaleGestureDetector.isInProgress || e2.pointerCount >= 2) {
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
    override fun resetZoom(animate: Boolean) {
        mZoomHelper.reset(animate)
        onZoomStateChanged()
    }

    override fun isZoomed() = mZoomHelper.isZoomed()

    /**
     * 缩放状态变化回调
     *
     * 供子类刷新依赖缩放状态的 UI（如「还原屏幕」按钮）
     */
    protected open fun onZoomStateChanged() {}

    /**
     * 锁定状态变化回调
     *
     * 锁定时已保留当前缩放状态，仅放弃多指平移的重心跟踪基准，
     * 避免解锁后因基准点过期导致画面跳动。
     */
    override fun onLockStateChanged(isLocked: Boolean) {
        super.onLockStateChanged(isLocked)
        mPointerTracked = false
    }

    override fun onPopupModeChanged(isPopup: Boolean) {
        super.onPopupModeChanged(isPopup)
        resetZoom(false)
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