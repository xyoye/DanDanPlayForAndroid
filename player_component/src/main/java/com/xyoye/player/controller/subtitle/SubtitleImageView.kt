package com.xyoye.player.controller.subtitle

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.ui.SubtitleView
import com.xyoye.data_component.enums.PlayState
import com.xyoye.player.controller.video.InterControllerView
import com.xyoye.player.wrapper.ControlWrapper

/**
 * Created by xyoye on 2020/12/21.
 *
 * 仅用于显示ExoPlayer内置字幕中的图片字幕
 */

class SubtitleImageView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), InterControllerView {

    private lateinit var mControlWrapper: ControlWrapper
    private val subtitleView = SubtitleView(context)

    private var lastCues: List<Cue>? = null
    private var mSubtitleEnable = false

    init {
        subtitleView.setUserDefaultStyle()
        subtitleView.setUserDefaultTextSize()
        addView(subtitleView)
    }

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
        var videoWidth = videoSize.x
        var videoHeight = videoSize.y

        videoWidth = if (videoWidth == 0) LayoutParams.MATCH_PARENT else videoWidth
        videoHeight = if (videoHeight == 0) LayoutParams.MATCH_PARENT else videoHeight

        subtitleView.layoutParams = LayoutParams(videoWidth, videoHeight).apply {
            gravity = Gravity.CENTER
        }
    }

    override fun onPopupModeChanged(isPopup: Boolean) {

    }

    fun setSubtitleEnable(enable: Boolean) {
        if (!enable) {
            setSubtitle(null)
        }
        mSubtitleEnable = enable
    }

    fun isEmptySubtitle() = lastCues.isNullOrEmpty()

    fun setSubtitle(cues: List<Cue>?) {
        if (mSubtitleEnable) {
            lastCues = cues
            subtitleView.setCues(cues)
        }
    }

    fun release() {
        lastCues = null
        mSubtitleEnable = false
    }
}