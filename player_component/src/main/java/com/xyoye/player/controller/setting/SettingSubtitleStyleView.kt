package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.core.view.isVisible
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.observeProgressChange
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSettingSubtitleStyleBinding


/**
 * Created by xyoye on 2022/1/10
 */

class SettingSubtitleStyleView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingSubtitleStyleBinding>(context, attrs, defStyleAttr) {

    init {
        initSettingListener()
    }

    override fun getLayoutId() = R.layout.layout_setting_subtitle_style

    override fun getSettingViewType() = SettingViewType.SUBTITLE_STYLE

    override fun onViewShow() {
        applySubtitleStyleStatus()
    }

    override fun onViewHide() {
        viewBinding.subtitleSettingNsv.focusedChild?.clearFocus()
    }

    override fun onViewShowed() {
        viewBinding.subtitleSizeSb.requestFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isSettingShowing().not()) {
            return false
        }

        handleKeyCode(keyCode)
        return true
    }

    private fun applySubtitleStyleStatus() {
        //文字大小
        val textSizePercent = PlayerInitializer.Subtitle.textSize
        val textSizeText = "$textSizePercent%"
        viewBinding.subtitleSizeTv.text = textSizeText
        viewBinding.subtitleSizeSb.progress = textSizePercent

        //描边宽度
        val strokeWidthPercent = PlayerInitializer.Subtitle.strokeWidth
        val strokeWidthText = "$strokeWidthPercent%"
        viewBinding.subtitleStrokeWidthTv.text = strokeWidthText
        viewBinding.subtitleStrokeWidthSb.progress = strokeWidthPercent

        //文字颜色
        viewBinding.subtitleColorSb.post {
            val textColor = PlayerInitializer.Subtitle.textColor
            viewBinding.subtitleColorSb.seekToColor(textColor)
            val textColorPosition = viewBinding.subtitleColorSb.getPositionFromColor(textColor)
            val textColorText = "$textColorPosition%"
            viewBinding.subtitleColorTv.text = textColorText
        }

        //描边颜色
        viewBinding.subtitleStrokeColorSb.post {
            val strokeColor = PlayerInitializer.Subtitle.strokeColor
            viewBinding.subtitleStrokeColorSb.seekToColor(strokeColor)
            val strokeColorPosition =
                viewBinding.subtitleStrokeColorSb.getPositionFromColor(strokeColor)
            val strokeColorText = "$strokeColorPosition%"
            viewBinding.subtitleStrokeColorTv.text = strokeColorText
        }

        viewBinding.tvResetSubtitleConfig.isVisible = isConfigChanged()
    }

    private fun initSettingListener() {
        viewBinding.tvResetSubtitleConfig.setOnClickListener {
            resetConfig()
        }

        viewBinding.subtitleSizeSb.observeProgressChange {
            updateSize(it)
        }

        viewBinding.subtitleStrokeWidthSb.observeProgressChange {
            updateStrokeWidth(it)
        }

        viewBinding.subtitleColorSb.setOnColorChangeListener { position, color ->
            updateTextColor(position, color)
        }

        viewBinding.subtitleStrokeColorSb.setOnColorChangeListener { position, color ->
            updateStrokeColor(position, color)
        }
    }

    private fun updateSize(progress: Int) {
        if (PlayerInitializer.Subtitle.textSize == progress)
            return

        val progressText = "$progress%"
        viewBinding.subtitleSizeTv.text = progressText
        viewBinding.subtitleSizeSb.progress = progress

        SubtitleConfig.putTextSize(progress)
        PlayerInitializer.Subtitle.textSize = progress
        mControlWrapper.updateTextSize()
        onConfigChanged()
    }

    private fun updateStrokeWidth(progress: Int) {
        if (PlayerInitializer.Subtitle.strokeWidth == progress)
            return

        val progressText = "$progress%"
        viewBinding.subtitleStrokeWidthTv.text = progressText
        viewBinding.subtitleStrokeWidthSb.progress = progress

        SubtitleConfig.putStrokeWidth(progress)
        PlayerInitializer.Subtitle.strokeWidth = progress
        mControlWrapper.updateStrokeWidth()
        onConfigChanged()
    }

    private fun updateTextColor(position: Int, color: Int, isFromUser: Boolean = true) {
        if (PlayerInitializer.Subtitle.textColor == color)
            return

        val progressText = "$position%"
        viewBinding.subtitleColorTv.text = progressText
        if (isFromUser.not()) {
            viewBinding.subtitleColorSb.seekTo(position)
        }

        SubtitleConfig.putTextColor(color)
        PlayerInitializer.Subtitle.textColor = color
        mControlWrapper.updateTextColor()
        onConfigChanged()
    }

    private fun updateStrokeColor(position: Int, color: Int, isFromUser: Boolean = true) {
        if (PlayerInitializer.Subtitle.strokeColor == color)
            return

        val progressText = "$position%"
        viewBinding.subtitleStrokeColorTv.text = progressText
        if (isFromUser.not()) {
            viewBinding.subtitleStrokeColorSb.seekTo(position)
        }

        SubtitleConfig.putStrokeColor(color)
        PlayerInitializer.Subtitle.strokeColor = color
        mControlWrapper.updateStrokeColor()
        onConfigChanged()
    }

    private fun resetConfig() {
        updateSize(PlayerInitializer.Subtitle.DEFAULT_SIZE)
        updateStrokeWidth(PlayerInitializer.Subtitle.DEFAULT_STROKE)

        val defaultTextColor = PlayerInitializer.Subtitle.DEFAULT_TEXT_COLOR
        val textColorPosition = viewBinding.subtitleColorSb.getPositionFromColor(defaultTextColor)
        updateTextColor(textColorPosition, defaultTextColor, isFromUser = false)

        val defaultStrokeColor = PlayerInitializer.Subtitle.DEFAULT_STROKE_COLOR
        val strokePosition =
            viewBinding.subtitleStrokeColorSb.getPositionFromColor(defaultStrokeColor)
        updateStrokeColor(strokePosition, defaultStrokeColor, isFromUser = false)
    }

    private fun onConfigChanged() {
        viewBinding.tvResetSubtitleConfig.isVisible = isConfigChanged()
    }

    private fun isConfigChanged(): Boolean {
        return PlayerInitializer.Subtitle.textSize != PlayerInitializer.Subtitle.DEFAULT_SIZE
                || PlayerInitializer.Subtitle.strokeWidth != PlayerInitializer.Subtitle.DEFAULT_STROKE
                || PlayerInitializer.Subtitle.textColor != PlayerInitializer.Subtitle.DEFAULT_TEXT_COLOR
                || PlayerInitializer.Subtitle.strokeColor != PlayerInitializer.Subtitle.DEFAULT_STROKE_COLOR
    }

    private fun handleKeyCode(keyCode: Int) {
        if (viewBinding.tvResetSubtitleConfig.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.subtitleStrokeColorSb.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.subtitleSizeSb.requestFocus()
            }
        } else if (viewBinding.subtitleSizeSb.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (isConfigChanged()) {
                        viewBinding.tvResetSubtitleConfig.requestFocus()
                    } else {
                        viewBinding.subtitleStrokeColorSb.requestFocus()
                    }
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.subtitleStrokeWidthSb.requestFocus()
            }
        } else if (viewBinding.subtitleStrokeWidthSb.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.subtitleSizeSb.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.subtitleColorSb.requestFocus()
            }
        } else if (viewBinding.subtitleColorSb.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> viewBinding.subtitleColorSb.previousPosition()
                KeyEvent.KEYCODE_DPAD_RIGHT -> viewBinding.subtitleColorSb.nextPosition()
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.subtitleStrokeWidthSb.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.subtitleStrokeColorSb.requestFocus()
            }
        } else if (viewBinding.subtitleStrokeColorSb.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> viewBinding.subtitleStrokeColorSb.previousPosition()
                KeyEvent.KEYCODE_DPAD_RIGHT -> viewBinding.subtitleStrokeColorSb.nextPosition()
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.subtitleColorSb.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (isConfigChanged()) {
                        viewBinding.tvResetSubtitleConfig.requestFocus()
                    } else {
                        viewBinding.subtitleSizeSb.requestFocus()
                    }
                }
            }
        } else {
            viewBinding.subtitleSizeSb.requestFocus()
        }
    }
}