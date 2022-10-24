package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.core.view.isVisible
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.extension.observeProgressChange
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSettingDanmuStyleBinding


/**
 * Created by xyoye on 2022/1/10
 */

class SettingDanmuStyleView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingDanmuStyleBinding>(context, attrs, defStyleAttr) {

    init {
        initSettingListener()
    }

    override fun getLayoutId() = R.layout.layout_setting_danmu_style

    override fun getSettingViewType() = SettingViewType.DANMU_STYLE

    override fun onViewShow() {
        applyDanmuStyleStatus()
    }

    override fun onViewHide() {
        viewBinding.playerSettingNsv.focusedChild?.clearFocus()
    }

    override fun onViewShowed() {
        viewBinding.danmuSizeSb.requestFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isSettingShowing().not()) {
            return false
        }

        handleKeyCode(keyCode)
        return true
    }

    private fun applyDanmuStyleStatus() {
        //文字大小
        val danmuSizePercent = PlayerInitializer.Danmu.size
        val danmuSizeText = "$danmuSizePercent%"
        viewBinding.danmuSizeTv.text = danmuSizeText
        viewBinding.danmuSizeSb.progress = danmuSizePercent

        //弹幕速度
        val danmuSpeedPercent = PlayerInitializer.Danmu.speed
        val danmuSpeedText = "$danmuSpeedPercent%"
        viewBinding.danmuSpeedTv.text = danmuSpeedText
        viewBinding.danmuSpeedSb.progress = danmuSpeedPercent

        //弹幕透明度
        val danmuAlphaPercent = PlayerInitializer.Danmu.alpha
        val danmuAlphaText = "$danmuAlphaPercent%"
        viewBinding.danmuAlphaTv.text = danmuAlphaText
        viewBinding.danmuAlphaSb.progress = danmuAlphaPercent

        //弹幕描边宽度
        val danmuStokePercent = PlayerInitializer.Danmu.stoke
        val danmuStokeText = "$danmuStokePercent%"
        viewBinding.danmuStokeTv.text = danmuStokeText
        viewBinding.danmuStokeSb.progress = danmuStokePercent

        viewBinding.tvResetDanmuConfig.isVisible = isConfigChanged()
    }

    private fun initSettingListener() {
        viewBinding.tvResetDanmuConfig.setOnClickListener {
            resetConfig()
        }

        viewBinding.danmuSizeSb.observeProgressChange {
            updateSize(it)
        }

        viewBinding.danmuSpeedSb.observeProgressChange {
            updateSpeed(it)
        }

        viewBinding.danmuAlphaSb.observeProgressChange {
            updateAlpha(it)
        }

        viewBinding.danmuStokeSb.observeProgressChange {
            updateStroke(it)
        }
    }

    private fun updateSize(progress: Int) {
        if (PlayerInitializer.Danmu.size == progress)
            return

        val progressText = "$progress%"
        viewBinding.danmuSizeTv.text = progressText
        viewBinding.danmuSizeSb.progress = progress

        DanmuConfig.putDanmuSize(progress)
        PlayerInitializer.Danmu.size = progress
        mControlWrapper.updateDanmuSize()
        onConfigChanged()
    }

    private fun updateSpeed(progress: Int) {
        if (PlayerInitializer.Danmu.speed == progress)
            return

        val progressText = "$progress%"
        viewBinding.danmuSpeedTv.text = progressText
        viewBinding.danmuSpeedSb.progress = progress

        DanmuConfig.putDanmuSpeed(progress)
        PlayerInitializer.Danmu.speed = progress
        mControlWrapper.updateDanmuSpeed()
        onConfigChanged()
    }

    private fun updateAlpha(progress: Int) {
        if (PlayerInitializer.Danmu.alpha == progress)
            return

        val progressText = "$progress%"
        viewBinding.danmuAlphaTv.text = progressText
        viewBinding.danmuAlphaSb.progress = progress

        DanmuConfig.putDanmuAlpha(progress)
        PlayerInitializer.Danmu.alpha = progress
        mControlWrapper.updateDanmuAlpha()
        onConfigChanged()
    }

    private fun updateStroke(progress: Int) {
        if (PlayerInitializer.Danmu.stoke == progress)
            return

        val progressText = "$progress%"
        viewBinding.danmuStokeTv.text = progressText
        viewBinding.danmuStokeSb.progress = progress

        DanmuConfig.putDanmuStoke(progress)
        PlayerInitializer.Danmu.stoke = progress
        mControlWrapper.updateDanmuStoke()
        onConfigChanged()
    }

    private fun resetConfig() {
        updateSize(PlayerInitializer.Danmu.DEFAULT_SIZE)
        updateSpeed(PlayerInitializer.Danmu.DEFAULT_SPEED)
        updateAlpha(PlayerInitializer.Danmu.DEFAULT_ALPHA)
        updateStroke(PlayerInitializer.Danmu.DEFAULT_STOKE)
    }

    private fun onConfigChanged() {
        viewBinding.tvResetDanmuConfig.isVisible = isConfigChanged()
    }

    private fun isConfigChanged(): Boolean {
        return PlayerInitializer.Danmu.offsetPosition != PlayerInitializer.Danmu.DEFAULT_POSITION
                || PlayerInitializer.Danmu.size != PlayerInitializer.Danmu.DEFAULT_SIZE
                || PlayerInitializer.Danmu.alpha != PlayerInitializer.Danmu.DEFAULT_ALPHA
                || PlayerInitializer.Danmu.stoke != PlayerInitializer.Danmu.DEFAULT_STOKE
                || PlayerInitializer.Danmu.speed != PlayerInitializer.Danmu.DEFAULT_SPEED
    }

    private fun handleKeyCode(keyCode: Int) {
        if (viewBinding.tvResetDanmuConfig.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.danmuStokeSb.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.danmuSizeSb.requestFocus()
            }
        } else if (viewBinding.danmuSizeSb.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (isConfigChanged()) {
                        viewBinding.tvResetDanmuConfig.requestFocus()
                    } else {
                        viewBinding.danmuSpeedSb.requestFocus()
                    }
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.danmuSpeedSb.requestFocus()
            }
        } else if (viewBinding.danmuSpeedSb.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.danmuSizeSb.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.danmuAlphaSb.requestFocus()
            }
        } else if (viewBinding.danmuAlphaSb.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.danmuSpeedSb.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.danmuStokeSb.requestFocus()
            }
        } else if (viewBinding.danmuStokeSb.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.danmuAlphaSb.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (isConfigChanged()) {
                        viewBinding.tvResetDanmuConfig.requestFocus()
                    } else {
                        viewBinding.danmuSizeSb.requestFocus()
                    }
                }
            }
        } else {
            viewBinding.danmuSizeSb.requestFocus()
        }
    }
}