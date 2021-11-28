package com.xyoye.player.controller.video

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.data_component.enums.PlayState
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSkipPositionBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by xyoye on 2021/2/22.
 */

class SkipPositionView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), InterControllerView {
    private lateinit var mControlWrapper: ControlWrapper

    private val attachLifecycle = (context as LifecycleOwner)

    private val viewBinding = DataBindingUtil.inflate<LayoutSkipPositionBinding>(
        LayoutInflater.from(context),
        R.layout.layout_skip_position,
        this,
        true
    )

    private val mDefaultTimeOutMs = 10 * 1000L

    private var skipPosition = 0L

    private var hideSkipJob: Job? = null

    init {
        viewBinding.skipCancelIv.setOnClickListener {
            toggleVisible(false)
        }

        viewBinding.skipConfirmTv.setOnClickListener {
            toggleVisible(false)
            mControlWrapper.seekTo(skipPosition)
        }
    }

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView() = this

    override fun onVisibilityChanged(isVisible: Boolean) {

    }

    override fun onPlayStateChanged(playState: PlayState) {
        if (playState == PlayState.STATE_PLAYING && skipPosition > 0) {
            toggleVisible(true)
            hideSkipJob?.cancel()
            hideSkipJob = attachLifecycle.lifecycleScope.launch {
                delay(mDefaultTimeOutMs)
                toggleVisible(false)
            }
        }

        //退出播放前移除延迟
        if (playState == PlayState.STATE_IDLE) {
            hideSkipJob?.cancel()
        }
    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {
        toggleVisible(true)
    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }

    fun setSkipPosition(position: Long) {
        skipPosition = position
        viewBinding.positionTv.text = formatDuration(position)
    }

    private fun toggleVisible(isVisible: Boolean) {
        if (isVisible) {
            viewBinding.skipPositionLl.transitionToEnd()
        } else {
            viewBinding.skipPositionLl.transitionToStart()
        }
    }

    fun release() {
        viewBinding.skipPositionLl.setProgress(0f, 0f)
        hideSkipJob?.cancel()
    }
}