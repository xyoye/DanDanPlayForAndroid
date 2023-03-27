package com.xyoye.player.controller.subtitle

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import com.xyoye.data_component.enums.PlayState
import com.xyoye.player.controller.video.InterControllerView
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.subtitle.BaseSubtitleView
import com.xyoye.subtitle.SubtitleText

/**
 * Created by xyoye on 2020/12/14.
 *
 * 用于显示所有文字字幕（外挂字幕、IJK内置字幕、EXO内置字幕）
 */

class SubtitleTextView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSubtitleView(context, attrs, defStyleAttr), InterControllerView {

    private lateinit var mControlWrapper: ControlWrapper

    //最后一次显示的字幕
    private var lastSubtitle: List<SubtitleText> = emptyList()

    init {
        updateTextSize()
        updateStrokeWidth()
        updateTextColor()
        updateStrokeColor()
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

    }

    override fun onPopupModeChanged(isPopup: Boolean) {
        //悬浮窗状态下，将字幕文字大小与描边缩小为原来的50%
        val textSize = PlayerInitializer.Subtitle.textSize
        var realSize = 40f * textSize / 100f
        if (isPopup) {
            realSize *= 0.5f
        }
        setTextSize(realSize.toInt())

        val strokeWidth = PlayerInitializer.Subtitle.strokeWidth
        var realWidth = 10f * strokeWidth / 100f
        if (isPopup) {
            realWidth *= 0.5f
        }
        setStrokeWidth(realWidth.toInt())
    }

    fun setSubtitle(subtitle: List<SubtitleText>?) {
        lastSubtitle = subtitle ?: emptyList()
        showSubtitle(lastSubtitle)
    }

    fun isEmptySubtitle() = lastSubtitle.isEmpty()

    fun updateTextSize() {
        val textSize = PlayerInitializer.Subtitle.textSize
        val realSize = (40f * textSize / 100f).toInt()
        setTextSize(realSize)
    }

    fun updateStrokeWidth() {
        val strokeWidth = PlayerInitializer.Subtitle.strokeWidth
        val realWidth = (10f * strokeWidth / 100f).toInt()
        setStrokeWidth(realWidth)
    }

    fun updateTextColor() {
        val textColor = PlayerInitializer.Subtitle.textColor
        setTextColor(textColor)
    }

    fun updateStrokeColor() {
        val strokeColor = PlayerInitializer.Subtitle.strokeColor
        setStrokeColor(strokeColor)
    }
}