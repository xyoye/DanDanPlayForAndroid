package com.xyoye.player.controller.subtitle

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xyoye.data_component.enums.PlayState
import com.xyoye.player.controller.video.InterControllerView
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.subtitle.*

/**
 * Created by xyoye on 2020/12/14.
 *
 * 用于显示所有文字字幕（外挂字幕、IJK内置字幕、EXO内置字幕）
 */

class SubtitleTextView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSubtitleView(context, attrs, defStyleAttr), InterControllerView, SubtitleOutput {
    //是否显示内置字幕
    private var mInnerSubtitleEnable = false
    //是否显示外挂字幕
    private var mExternalSubtitleEnable = false

    private lateinit var mControlWrapper: ControlWrapper
    //外挂字幕管理器
    private var mSubtitleManager = ExternalSubtitleManager()

    //最后一次显示的字幕
    private var lastSubtitle: String? = null

    //外挂字幕地址
    private var mUrl: String? = null

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
        if (mUrl == null)
            return
        if (!mExternalSubtitleEnable)
            return
        when (playState) {
            PlayState.STATE_IDLE -> {
                mSubtitleManager.observerOnSubtitleLoad(null)
                mSubtitleManager.stop()
            }
            PlayState.STATE_PLAYING -> {
                mSubtitleManager.start()
            }
            PlayState.STATE_COMPLETED,
            PlayState.STATE_ERROR,
            PlayState.STATE_PAUSED -> {
                mSubtitleManager.stop()
            }
            else -> {
            }
        }
    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {

    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }

    override fun onSubtitleOutput(subtitles: MutableList<SubtitleText>) {
        showSubtitle(subtitles)
    }

    override fun getCurrentPosition() = mControlWrapper.getCurrentPosition()

    fun getSubtitleManager() = mSubtitleManager

    fun setSubtitlePath(subtitlePath: String, playWhenReady: Boolean = false) {
        mUrl = subtitlePath
        val lifecycleScope = (context as AppCompatActivity).lifecycleScope
        mSubtitleManager.apply {
            bindOutput(lifecycleScope, this@SubtitleTextView)
            bindSource(subtitlePath, playWhenReady)
            stop()
        }
    }

    fun setSubtitle(subtitle: String?) {
        if (mInnerSubtitleEnable) {
            lastSubtitle = subtitle
            onSubtitleOutput(SubtitleUtils.caption2Subtitle(subtitle))
        }
    }

    fun isEmptySubtitle() = lastSubtitle.isNullOrEmpty()

    fun showExternalSubtitle() {
        if (!mExternalSubtitleEnable) {
            mExternalSubtitleEnable = true
            mSubtitleManager.start()
        }
        mInnerSubtitleEnable = false
    }

    fun showInnerSubtitle() {
        if (mExternalSubtitleEnable) {
            mExternalSubtitleEnable = false
            mSubtitleManager.stop()
        }
        mInnerSubtitleEnable = true
    }

    fun setSubtitleDisable() {
        setSubtitle(null)
        if (mExternalSubtitleEnable) {
            mExternalSubtitleEnable = false
            mSubtitleManager.stop()
        }
        mInnerSubtitleEnable = false
    }

    fun isExternalSubtitleEnable() = mExternalSubtitleEnable

    fun isInnerSubtitleEnable() = mInnerSubtitleEnable

    fun updateTextSize() {
        val textSize = PlayerInitializer.Subtitle.textSize
        setTextSize(textSize)
    }

    fun updateStrokeWidth() {
        val strokeWidth = PlayerInitializer.Subtitle.strokeWidth
        setStrokeWidth(strokeWidth)
    }

    fun updateTextColor() {
        val textColor = PlayerInitializer.Subtitle.textColor
        setTextColor(textColor)
    }

    fun updateStrokeColor() {
        val strokeColor = PlayerInitializer.Subtitle.strokeColor
        setStrokeColor(strokeColor)
    }

    fun updateOffsetTime() {
        val offsetTime = PlayerInitializer.Subtitle.offsetPosition
        mSubtitleManager.setOffset(offsetTime)
    }
}