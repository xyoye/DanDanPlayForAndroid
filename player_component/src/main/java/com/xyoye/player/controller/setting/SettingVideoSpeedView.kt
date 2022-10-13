package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.slider.LabelFormatter
import com.xyoye.common_component.config.PlayerConfig
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSettingVideoSpeedBinding

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/10/12
 *     desc  :
 * </pre>
 */

class SettingVideoSpeedView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingVideoSpeedBinding>(context, attrs, defStyleAttr) {

    //当前View可处理的事件
    private val handleKeyCodes = listOf(
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_CENTER
    )

    init {
        initVideoSpeed()
    }

    override fun getLayoutId() = R.layout.layout_setting_video_speed

    override fun getSettingViewType() = SettingViewType.VIDEO_SPEED

    override fun onViewShowed() {
        viewBinding.speedSlider.value = PlayerInitializer.Player.videoSpeed
        viewBinding.speedSlider.labelBehavior = LabelFormatter.LABEL_VISIBLE
        viewBinding.speedSlider.requestFocus()
    }

    override fun onViewHide() {
        viewBinding.speedSlider.labelBehavior = LabelFormatter.LABEL_WITHIN_BOUNDS
        viewBinding.speedSlider.clearFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (handleKeyCodes.contains(keyCode).not()) {
            return super.onKeyDown(keyCode, event)
        }

        if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER
            && viewBinding.speedSlider.hasFocus().not()
        ) {
            viewBinding.speedSlider.requestFocus()
        } else if ((keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
            && viewBinding.speedSlider.hasFocus()
            && viewBinding.resetTv.isVisible
        ) {
            viewBinding.resetTv.requestFocus()
        }
        return true
    }

    private fun initVideoSpeed() {
        viewBinding.speedSlider.addOnChangeListener { _, value, _ ->
            viewBinding.resetTv.isGone = value == PlayerInitializer.Player.DEFAULT_SPEED

            PlayerConfig.putNewVideoSpeed(value)
            PlayerInitializer.Player.videoSpeed = value
            mControlWrapper.setSpeed(value)
        }

        viewBinding.resetTv.setOnClickListener {
            viewBinding.speedSlider.value = PlayerInitializer.Player.DEFAULT_SPEED
            viewBinding.speedSlider.requestFocus()
        }
    }
}