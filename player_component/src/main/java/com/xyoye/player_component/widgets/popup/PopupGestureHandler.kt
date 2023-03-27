package com.xyoye.player_component.widgets.popup

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.getScreenHeight
import com.xyoye.common_component.utils.getScreenWidth


/**
 * Created by xyoye on 2022/11/10.
 */

@SuppressLint("ClickableViewAccessibility")
class PopupGestureHandler(
    private val viewPosition: PopupPositionListener
) : View.OnTouchListener {

    companion object {
        val POPUP_MARGIN_X = dp2px(10)
        val POPUP_MARGIN_Y = dp2px(50)
    }

    private var lastX = 0f
    private var lastY = 0f

    private var changeX = 0f
    private var changeY = 0f

    private var newX = 0
    private var newY = 0

    private var mAnimator: ValueAnimator? = null

    private val mInterpolator = DecelerateInterpolator()

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
                cancelAnimator()
            }
            MotionEvent.ACTION_MOVE -> {
                changeX = event.rawX - lastX
                changeY = event.rawY - lastY

                val position = viewPosition.getPosition()
                newX = (position.x + changeX).toInt()
                newY = (position.y + changeY).toInt()

                viewPosition.setPosition(Point(newX, newY))

                lastX = event.rawX
                lastY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                correctPosition(v.context, v.width, v.height)
            }

        }
        return true
    }

    fun reset() {
        lastX = 0f
        lastY = 0f
        changeX = 0f
        changeY = 0f
        newX = 0
        newY = 0

        mAnimator?.apply {
            cancel()
            removeAllUpdateListeners()
            removeAllListeners()
        }
        mAnimator = null
    }

    /**
     * 位置修正
     */
    fun correctPosition(context: Context, viewWidth: Int, viewHeight: Int) {
        val screenWidth = context.applicationContext.getScreenWidth(false)
        val screenHeight = context.applicationContext.getScreenHeight(false)

        val startX = viewPosition.getPosition().x
        val endX = if (startX * 2 + viewWidth > screenWidth) {
            // 超出屏幕一半时靠右
            screenWidth - viewWidth - POPUP_MARGIN_X
        } else {
            // 靠左
            POPUP_MARGIN_X
        }

        val startY = viewPosition.getPosition().y
        var endY = startY
        if (endY > screenHeight - viewHeight - POPUP_MARGIN_Y) {
            // 与底部需要有一定的间距
            endY = screenHeight - viewHeight - POPUP_MARGIN_Y
        } else if (endY < POPUP_MARGIN_Y) {
            // 与顶部需要有一定的间距
            endY = POPUP_MARGIN_Y
        }
        val startPoint = Point(startX, startY)
        val endPoint = Point(endX, endY)

        mAnimator = ValueAnimator.ofObject(PointEvaluator(), startPoint, endPoint).apply {
            addUpdateListener {
                viewPosition.setPosition(it.animatedValue as Point)
            }
        }
        startAnimator()
    }

    private fun startAnimator() {
        mAnimator?.apply {
            duration = 300
            interpolator = mInterpolator
            addListener(onEnd = {
                removeAllUpdateListeners()
                removeAllListeners()
                mAnimator = null
            })
            start()
        }
    }

    private fun cancelAnimator() {
        if (mAnimator?.isRunning == true) {
            mAnimator!!.cancel()
        }
    }
}