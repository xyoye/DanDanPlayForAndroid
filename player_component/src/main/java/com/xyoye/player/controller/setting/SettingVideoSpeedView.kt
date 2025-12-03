package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.slider.LabelFormatter
import com.xyoye.common_component.config.PlayerConfig
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSettingVideoSpeedBinding
import kotlin.math.roundToInt

/**
 * Created by xyoye on 2022/10/12
 */

class SettingVideoSpeedView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingVideoSpeedBinding>(context, attrs, defStyleAttr) {

    private val focusableViews by lazy {
        arrayOf(
            viewBinding.resetTv,
            viewBinding.speedSlider,
            viewBinding.tvResetPressSpeed,
            viewBinding.sliderPressSpeed
        )
    }

    private val defaultFocusView by lazy { viewBinding.speedSlider }

    init {
        initVideoSpeed()
    }

    override fun getLayoutId() = R.layout.layout_setting_video_speed

    override fun getSettingViewType() = SettingViewType.VIDEO_SPEED

    override fun onViewShowed() {
        viewBinding.speedSlider.value = PlayerInitializer.Player.videoSpeed
        viewBinding.speedSlider.labelBehavior = LabelFormatter.LABEL_VISIBLE

        viewBinding.sliderPressSpeed.value = PlayerInitializer.Player.pressVideoSpeed
        viewBinding.sliderPressSpeed.labelBehavior = LabelFormatter.LABEL_VISIBLE

        viewBinding.speedSlider.requestFocus()
    }

    override fun onViewHide() {
        viewBinding.speedSlider.labelBehavior = LabelFormatter.LABEL_WITHIN_BOUNDS
        viewBinding.speedSlider.clearFocus()

        viewBinding.sliderPressSpeed.labelBehavior = LabelFormatter.LABEL_WITHIN_BOUNDS
        viewBinding.sliderPressSpeed.clearFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isSettingShowing().not()) {
            return false
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            findPreviousFocus().requestFocus()
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            findNextFocus().requestFocus()
        } else if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER) {
            viewBinding.speedSlider.requestFocus()
        }
        return true
    }

    private fun initVideoSpeed() {
        viewBinding.speedSlider.addOnChangeListener { _, approximation, _ ->
            val value = (approximation * 100).roundToInt() / 100f
            viewBinding.resetTv.isGone = value == PlayerInitializer.Player.DEFAULT_SPEED

            PlayerConfig.setNewVideoSpeed(value)
            PlayerInitializer.Player.videoSpeed = value
            mControlWrapper.setSpeed(value)
        }

        viewBinding.resetTv.setOnClickListener {
            viewBinding.speedSlider.value = PlayerInitializer.Player.DEFAULT_SPEED
            viewBinding.speedSlider.requestFocus()
        }

        viewBinding.sliderPressSpeed.addOnChangeListener { _, approximation, _ ->
            val value = (approximation * 100).roundToInt() / 100f
            viewBinding.tvResetPressSpeed.isGone = value == PlayerInitializer.Player.DEFAULT_PRESS_SPEED

            PlayerConfig.setPressVideoSpeed(value)
            PlayerInitializer.Player.pressVideoSpeed = value
        }

        viewBinding.tvResetPressSpeed.setOnClickListener {
            viewBinding.sliderPressSpeed.value = PlayerInitializer.Player.DEFAULT_PRESS_SPEED
            viewBinding.sliderPressSpeed.requestFocus()
        }
    }

    private fun findNextFocus(): View {
        val currentFocusView = focusableViews.firstOrNull { it.isFocused }
            ?: return defaultFocusView

        val nextFocusAbleViews = focusableViews.filter { it.isVisible }
        val index = nextFocusAbleViews.indexOf(currentFocusView)
        if (index == -1) {
            return defaultFocusView
        }

        if (index == nextFocusAbleViews.size - 1) {
            return nextFocusAbleViews.first()
        }
        return nextFocusAbleViews[index + 1]
    }

    private fun findPreviousFocus(): View {
        val currentFocusView = focusableViews.firstOrNull { it.isFocused }
            ?: return defaultFocusView

        val previousFocusAbleViews = focusableViews.filter { it.isVisible }
        val index = previousFocusAbleViews.indexOf(currentFocusView)
        if (index == -1) {
            return defaultFocusView
        }

        if (index == 0) {
            return previousFocusAbleViews.last()
        }
        return previousFocusAbleViews[index - 1]
    }
}