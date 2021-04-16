package com.xyoye.player.controller.video

import android.content.Context
import android.graphics.Point
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.data_component.enums.PlayState
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSkipPositionBinding

/**
 * Created by xyoye on 2021/2/22.
 */

class SkipPositionView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), InterControllerView {
    private lateinit var mControlWrapper: ControlWrapper

    private val viewBinding = DataBindingUtil.inflate<LayoutSkipPositionBinding>(
        LayoutInflater.from(context),
        R.layout.layout_skip_position,
        this,
        true
    )

    private val hideViewRunnable = {
        toggleVisible(false)
    }

    private var isShowed = false
    private var skipPosition = 0L

    init {
        post {
            //默认隐藏
            viewBinding.skipPositionLl.apply {
                translationX = -width.toFloat()
            }
        }

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
        if (playState == PlayState.STATE_PLAYING && !isShowed && skipPosition > 0) {
            isShowed = true
            post { toggleVisible(true) }
            //10秒后隐藏
            postDelayed(hideViewRunnable, 10 * 1000)
        }

        //退出播放前移除延迟
        if (playState == PlayState.STATE_IDLE) {
            val mainHandle: Handler? = handler
            mainHandle?.removeMessages(0)
        }
    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {
        toggleVisible(false)
    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }

    fun setSkipPosition(position: Long) {
        skipPosition = position
        viewBinding.positionTv.text = formatDuration(position)
    }

    private fun toggleVisible(isVisible: Boolean) {
        if (isVisible) {
            if (viewBinding.skipPositionLl.translationX != 0f) {
                ViewCompat.animate(viewBinding.skipPositionLl)
                    .translationX(0f)
                    .setDuration(300)
                    .start()
            }
        } else {
            if (viewBinding.skipPositionLl.translationX == 0f) {
                removeCallbacks(hideViewRunnable)
                ViewCompat.animate(viewBinding.skipPositionLl)
                    .translationX(-viewBinding.skipPositionLl.width.toFloat())
                    .setDuration(300)
                    .start()
            }
        }
    }
}