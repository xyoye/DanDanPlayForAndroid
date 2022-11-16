package com.xyoye.player_component.widgets.popup

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.getScreenWidth


/**
 * Created by xyoye on 2022/11/10.
 */

@SuppressLint("ClickableViewAccessibility")
class PopupGestureHandler(
    private val viewPosition: PopupPositionListener
) : View.OnTouchListener {

    companion object {
        val POPUP_MARGIN = dp2px(10)
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
                val screenWidth = v.context.applicationContext.getScreenWidth()

                val startX = viewPosition.getPosition().x
                var endX = POPUP_MARGIN
                if (startX * 2 + v.width > screenWidth) {
                    endX = screenWidth - v.width - POPUP_MARGIN
                }

                mAnimator = ObjectAnimator.ofInt(startX, endX).apply {
                    addUpdateListener {
                        val x = it.animatedValue as Int
                        val position = viewPosition.getPosition()
                        val newPosition = Point(x, position.y)
                        viewPosition.setPosition(newPosition)
                    }
                }
                startAnimator()
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