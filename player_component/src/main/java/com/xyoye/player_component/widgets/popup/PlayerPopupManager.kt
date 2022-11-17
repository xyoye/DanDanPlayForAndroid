package com.xyoye.player_component.widgets.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.OrientationEventListener
import android.view.ViewGroup
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.getScreenHeight
import com.xyoye.common_component.utils.getScreenWidth
import com.xyoye.player.DanDanVideoPlayer
import com.xyoye.player.utils.OrientationHelper

/**
 * Created by xyoye on 2022/11/3
 */

@SuppressLint("ClickableViewAccessibility")
class PlayerPopupManager(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr),
    PopupPositionListener,
    OrientationHelper.OnOrientationChangeListener {

    private val appContext = context.applicationContext

    private var mPosition = Point(0, 0)

    private val mPlayerLayoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    private val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val mWindowLayoutParams = WindowManager.LayoutParams().apply {

        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        windowAnimations = 0
        gravity = Gravity.TOP or Gravity.START
        format = PixelFormat.RGBA_8888
    }

    private val mGestureHandler = PopupGestureHandler(this)

    private var mOrientation: Int? = null
    private val mOrientationHelper = OrientationHelper(appContext)

    private var isShowing = false

    init {
        outlineProvider = PopupOutlineProvider(dp2px(5).toFloat())
        clipToOutline = true

        mOrientationHelper.mOnOrientationChangeListener = this
    }

    override fun setPosition(point: Point) {
        mPosition = point

        mWindowLayoutParams.x = point.x
        mWindowLayoutParams.y = point.y
        mWindowManager.updateViewLayout(this, mWindowLayoutParams)
    }

    override fun getPosition() = mPosition

    fun show(player: DanDanVideoPlayer) {
        if (isShowing) {
            return
        }
        mOrientationHelper.enable()
        mGestureHandler.reset()
        player.setPopupGestureHandler(mGestureHandler)

        removeAllViews()
        addView(player, mPlayerLayoutParams)

        //悬浮窗尺寸
        val popupSize = computerPopupSize(player.getVideoSize())
        //悬浮窗位置
        mPosition = Point(
            appContext.getScreenWidth(false) - popupSize.x - PopupGestureHandler.POPUP_MARGIN,
            ((appContext.getScreenHeight() - popupSize.y) * 0.5).toInt()
        )

        mWindowLayoutParams.apply {
            width = popupSize.x
            height = popupSize.y
            x = mPosition.x
            y = mPosition.y
        }
        mWindowManager.addView(this, mWindowLayoutParams)

        isShowing = true
    }

    fun dismiss() {
        if (isShowing.not()) {
            return
        }
        mOrientationHelper.disable()
        mGestureHandler.reset()
        if (childCount > 0) {
            val player = getChildAt(0)
            if (player is DanDanVideoPlayer) {
                player.setPopupGestureHandler(null)
            }
        }

        removeAllViews()
        mWindowManager.removeView(this)

        isShowing = false
    }

    fun isShowing() = isShowing

    private fun computerPopupSize(videoSize: Point): Point {
        val defaultWidth = dp2px(200)

        if (videoSize.x == 0 || videoSize.y == 0) {
            return Point(
                defaultWidth,
                defaultWidth * 3 / 4
            )
        }

        val present = defaultWidth / videoSize.x.toFloat()
        val height = videoSize.y * present
        return Point(defaultWidth, height.toInt())
    }

    override fun onOrientationChanged(orientation: Int) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN){
            return
        }

        if (isShowing.not()) {
            return
        }

        if (width == 0) {
            return
        }

        val currentOrientation = appContext.resources.configuration.orientation
        if (currentOrientation != mOrientation) {
            mOrientation = currentOrientation
            mGestureHandler.correctPosition(appContext, width)
        }
    }
}