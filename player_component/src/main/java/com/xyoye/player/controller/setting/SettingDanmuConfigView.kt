package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.extension.observeProgressChange
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSettingDanmuConfigBinding


/**
 * Created by xyoye on 2022/1/10
 */

class SettingDanmuConfigView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingDanmuConfigBinding>(context, attrs, defStyleAttr) {

    init {
        initSettingView()

        initSettingListener()
    }

    override fun getLayoutId() = R.layout.layout_setting_danmu_config

    override fun getSettingViewType() = SettingViewType.DANMU_SETTING_CONFIG

    private fun initSettingView() {
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

        //弹幕时间调节
        val extraPosition = PlayerInitializer.Danmu.offsetPosition / 1000f
        viewBinding.danmuExtraTimeEt.setText(extraPosition.toString())

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

        viewBinding.danmuExtraTimeReduce.setOnClickListener {
            updateOffsetPosition(PlayerInitializer.Danmu.offsetPosition - 500)
        }

        viewBinding.danmuExtraTimeAdd.setOnClickListener {
            updateOffsetPosition(PlayerInitializer.Danmu.offsetPosition + 500)
        }

        viewBinding.danmuExtraTimeEt.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val extraTimeText = viewBinding.danmuExtraTimeEt.text.toString()
                val newOffsetSecond = extraTimeText.toFloatOrNull() ?: 0f
                updateOffsetPosition((newOffsetSecond * 1000).toLong())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
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

    private fun updateOffsetPosition(position: Long) {
        if (PlayerInitializer.Danmu.offsetPosition == position)
            return

        hideKeyboard(viewBinding.danmuExtraTimeEt)
        viewBinding.danmuOffsetTimeLl.requestFocus()

        PlayerInitializer.Danmu.offsetPosition = position
        val offsetSecond = PlayerInitializer.Danmu.offsetPosition / 1000f
        viewBinding.danmuExtraTimeEt.setText(offsetSecond.toString())
        mControlWrapper.updateOffsetTime()
        onConfigChanged()
    }

    private fun resetConfig() {
        updateSize(PlayerInitializer.Danmu.DEFAULT_SIZE)
        updateSpeed(PlayerInitializer.Danmu.DEFAULT_SPEED)
        updateAlpha(PlayerInitializer.Danmu.DEFAULT_ALPHA)
        updateStroke(PlayerInitializer.Danmu.DEFAULT_STOKE)
        updateOffsetPosition(PlayerInitializer.Danmu.DEFAULT_POSITION)
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
}