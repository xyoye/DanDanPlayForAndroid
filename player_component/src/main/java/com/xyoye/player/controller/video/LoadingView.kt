package com.xyoye.player.controller.video

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.xyoye.common_component.utils.dp2px
import com.xyoye.data_component.enums.PlayState
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutLoadingBinding

/**
 * Created by xyoye on 2021/1/22.
 */

class LoadingView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), InterControllerView {

    private val viewBinding = DataBindingUtil.inflate<LayoutLoadingBinding>(
        LayoutInflater.from(context),
        R.layout.layout_loading,
        this,
        true
    )

    private val cycleAnimator = ObjectAnimator
        .ofFloat(viewBinding.loadingIv, "rotation", 360f).apply {
            duration = 1500
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
        }


    override fun attach(controlWrapper: ControlWrapper) {

    }

    override fun getView() = this

    override fun onVisibilityChanged(isVisible: Boolean) {

    }

    override fun onPlayStateChanged(playState: PlayState) {
        when (playState) {
            PlayState.STATE_COMPLETED,
            PlayState.STATE_ERROR,
            PlayState.STATE_START_ABORT,
            PlayState.STATE_PLAYING,
            PlayState.STATE_IDLE,
            PlayState.STATE_BUFFERING_PLAYING,
            PlayState.STATE_PREPARED -> {
                hideLoading()
            }
            PlayState.STATE_BUFFERING_PAUSED,
            PlayState.STATE_PREPARING -> {
                showLoading()
            }
            else -> {}
        }
    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {

    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }

    override fun onPopupModeChanged(isPopup: Boolean) {
        var loadingSize = dp2px(60)
        if (isPopup) {
            loadingSize /= 2
        }

        val layoutParams = viewBinding.loadingIv.layoutParams
        layoutParams.width = loadingSize
        layoutParams.height = loadingSize
        viewBinding.loadingIv.layoutParams = layoutParams
    }

    private fun showLoading() {
        if (viewBinding.loadingIv.isGone) {
            viewBinding.loadingIv.isGone = false
            cycleAnimator.start()
        }
    }

    private fun hideLoading() {
        if (viewBinding.loadingIv.isVisible) {
            viewBinding.loadingIv.isVisible = false
            cycleAnimator.cancel()
        }
    }
}