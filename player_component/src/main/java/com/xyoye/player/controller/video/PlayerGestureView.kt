package com.xyoye.player.controller.video

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListenerAdapter
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.xyoye.data_component.enums.PlayState
import com.xyoye.player.utils.formatDuration
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutPlayerGestureBinding

/**
 * Created by xyoye on 2020/11/13.
 */

class PlayerGestureView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), InterGestureView {

    private lateinit var mControlWrapper: ControlWrapper

    private val mFadeGestureOut = Runnable { hideGestureView() }
    private val viewBinding = DataBindingUtil.inflate<LayoutPlayerGestureBinding>(
        LayoutInflater.from(context),
        R.layout.layout_player_gesture,
        this,
        true
    )

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView() = this

    override fun onVisibilityChanged(isVisible: Boolean) {

    }

    override fun onPlayStateChanged(playState: PlayState) {

    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {

    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }

    override fun onStartSlide() {
        removeCallbacks(mFadeGestureOut)
        mControlWrapper.hideController()
        viewBinding.gestureContainer.isVisible = true
        viewBinding.gestureContainer.alpha = 1f
    }

    override fun onStopSlide() {
        removeCallbacks(mFadeGestureOut)
        postDelayed(mFadeGestureOut, 800)
    }

    override fun onPositionChange(newPosition: Long, currentPosition: Long, duration: Long) {
        viewBinding.positionTv.isVisible = true
        viewBinding.volumeTv.isVisible = false
        viewBinding.batteryTv.isVisible = false

        val durationFormat = formatDuration(duration)
        val newPositionFormat =
            formatDuration(newPosition)

        val updateSecond: Int = ((newPosition - currentPosition) / 1000f).toInt()
        val updateSecondText = if (updateSecond > 0) "+$updateSecond" else updateSecond.toString()

        val updateText = "$newPositionFormat/$durationFormat\n${updateSecondText}秒"
        viewBinding.positionTv.text = updateText
    }

    override fun onBrightnessChange(percent: Int) {
        viewBinding.batteryTv.isVisible = true
        viewBinding.volumeTv.isVisible = false
        viewBinding.positionTv.isVisible = false

        val batteryText = "$percent%"
        viewBinding.batteryTv.text = batteryText
    }

    override fun onVolumeChange(percent: Int) {
        viewBinding.positionTv.isVisible = false
        viewBinding.volumeTv.isVisible = true
        viewBinding.batteryTv.isVisible = false

        val volumeText = "$percent%"
        viewBinding.volumeTv.text = volumeText
    }


    private fun hideGestureView() {
        ViewCompat.animate(viewBinding.gestureContainer)
            .alpha(0f)
            .setDuration(150)
            .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                override fun onAnimationEnd(view: View?) {
                    super.onAnimationEnd(view)
                    viewBinding.gestureContainer.isVisible = false
                    viewBinding.positionTv.isVisible = false
                    viewBinding.batteryTv.isVisible = false
                    viewBinding.volumeTv.isVisible = false
                }
            }).start()
    }
}