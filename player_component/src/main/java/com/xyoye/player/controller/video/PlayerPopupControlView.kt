package com.xyoye.player.controller.video

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.xyoye.data_component.enums.PlayState
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutPlayerPopupControlBinding

/**
 * Created by xyoye on 2022/11/10.
 */

class PlayerPopupControlView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), InterControllerView {
    //悬浮窗关闭回调
    private var mPopupDismissBlock: (() -> Unit)? = null

    //悬浮窗展开回调
    private var mPopupExpandBlock:(() -> Unit)? = null

    private val viewBinding = DataBindingUtil.inflate<LayoutPlayerPopupControlBinding>(
        LayoutInflater.from(context),
        R.layout.layout_player_popup_control,
        this,
        true
    )

    private lateinit var mControlWrapper: ControlWrapper

    init {
        viewBinding.ivClose.setOnClickListener {
            mPopupDismissBlock?.invoke()
        }

        viewBinding.ivDanmuControl.setOnClickListener {
            mControlWrapper.toggleDanmuVisible()
            viewBinding.ivDanmuControl.isSelected = !viewBinding.ivDanmuControl.isSelected
        }

        viewBinding.ivExpand.setOnClickListener {
            mPopupExpandBlock?.invoke()
        }

        viewBinding.ivPlay.setOnClickListener {
            mControlWrapper.togglePlay()
        }

        viewBinding.ivSeekForward.setOnClickListener {
            val currentPosition = mControlWrapper.getCurrentPosition()
            val duration = mControlWrapper.getDuration()
            val newPosition = currentPosition + (15 * 1000)
            if (newPosition < duration) {
                mControlWrapper.seekTo(newPosition)
            }
        }

        viewBinding.ivSeekBack.setOnClickListener {
            val currentPosition = mControlWrapper.getCurrentPosition()
            val newPosition = currentPosition - (15 * 1000)
            if (newPosition > 0) {
                mControlWrapper.seekTo(newPosition)
            } else {
                mControlWrapper.seekTo(0)
            }
        }
    }

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView(): View {
        return this
    }

    override fun onVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            ViewCompat.animate(viewBinding.settingLayout)
                .alpha(1f)
                .setDuration(300)
                .withStartAction { viewBinding.settingLayout.isVisible = true }
                .start()
        } else {
            ViewCompat.animate(viewBinding.settingLayout)
                .alpha(0f)
                .setDuration(300)
                .withEndAction { viewBinding.settingLayout.isVisible = false }
                .start()
        }
    }

    override fun onPlayStateChanged(playState: PlayState) {
        when (playState) {
            PlayState.STATE_IDLE -> {
                viewBinding.playProgress.progress = 0
                viewBinding.playProgress.secondaryProgress = 0
            }
            PlayState.STATE_PREPARING -> {
                viewBinding.ivPlay.isSelected = false
            }
            PlayState.STATE_START_ABORT,
            PlayState.STATE_PREPARED,
            PlayState.STATE_PAUSED,
            PlayState.STATE_ERROR -> {
                viewBinding.ivPlay.isSelected = false
                mControlWrapper.stopProgress()
            }
            PlayState.STATE_PLAYING -> {
                viewBinding.ivPlay.isSelected = true
                mControlWrapper.startProgress()
            }
            PlayState.STATE_BUFFERING_PAUSED,
            PlayState.STATE_BUFFERING_PLAYING -> {
                viewBinding.ivPlay.isSelected = mControlWrapper.isPlaying()
            }
            PlayState.STATE_COMPLETED -> {
                mControlWrapper.stopProgress()
                viewBinding.ivPlay.isSelected = mControlWrapper.isPlaying()
            }
        }
    }

    override fun onProgressChanged(duration: Long, position: Long) {
        if (duration > 0) {
            viewBinding.playProgress.progress =
                (position.toFloat() / duration * viewBinding.playProgress.max).toInt()
        }

        var bufferedPercent = mControlWrapper.getBufferedPercentage()
        if (bufferedPercent > 95)
            bufferedPercent = 100
        viewBinding.playProgress.secondaryProgress = bufferedPercent
    }

    override fun onLockStateChanged(isLocked: Boolean) {

    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }

    override fun onPopupModeChanged(isPopup: Boolean) {

    }

    fun setPopupDismissObserver(block: () -> Unit) {
        mPopupDismissBlock = block
    }

    fun setPopupExpandObserver(block: () -> Unit) {
        mPopupExpandBlock = block
    }
}