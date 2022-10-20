package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSettingDanmuConfigureBinding
import master.flame.danmaku.danmaku.model.BaseDanmaku

/**
 * Created by xyoye on 2022/10/18
 */

class SettingDanmuConfigureView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingDanmuConfigureBinding>(context, attrs, defStyleAttr) {

    private var settingMode = BaseDanmaku.TYPE_SCROLL_RL

    init {
        initView()
    }

    override fun getLayoutId() = R.layout.layout_setting_danmu_configure

    override fun getSettingViewType() = SettingViewType.DANMU_CONFIGURE

    override fun onViewShow() {
        applyDanmuConfigureStatus()
    }

    override fun onViewHide() {
        hideKeyboard(viewBinding.etScreenMaxNum)
        hideKeyboard(viewBinding.etMaxLine)
        viewBinding.playerSettingNsv.focusedChild?.clearFocus()
    }

    override fun onViewShowed() {
        viewBinding.llScrollDanmu.requestFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isSettingShowing().not()) {
            return false
        }

        handleKeyCode(keyCode)
        return true
    }

    private fun initView() {
        viewBinding.tvKeywordBlock.setOnClickListener {
            onSettingVisibilityChanged(false)
            mControlWrapper.showSettingView(SettingViewType.KEYWORD_BLOCK)
        }

        viewBinding.llScrollDanmu.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                settingMode = BaseDanmaku.TYPE_SCROLL_RL
                applyDanmuConfigureStatus()
            }
        }

        viewBinding.llTopDanmu.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                settingMode = BaseDanmaku.TYPE_FIX_TOP
                applyDanmuConfigureStatus()
            }
        }

        viewBinding.llBottomDanmu.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                settingMode = BaseDanmaku.TYPE_FIX_BOTTOM
                applyDanmuConfigureStatus()
            }
        }

        viewBinding.switchDanmuEnable.setOnCheckedChangeListener { _, isChecked ->
            updateDanmuEnable(isChecked)
        }

        viewBinding.tvLineNoLimit.setOnClickListener {
            viewBinding.etMaxLine.clearFocus()
            updateMaxLine(PlayerInitializer.Danmu.DEFAULT_MAX_LINE)
        }

        viewBinding.etMaxLine.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //输入为空或0，设置为无限制
                val maxLineText = viewBinding.etMaxLine.text.toString()
                var newMaxLine = maxLineText.toIntOrNull()
                    ?: PlayerInitializer.Danmu.DEFAULT_MAX_LINE
                newMaxLine =
                    if (newMaxLine <= 0) PlayerInitializer.Danmu.DEFAULT_MAX_LINE else newMaxLine

                updateMaxLine(newMaxLine)
                hideKeyboard(viewBinding.etMaxLine)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        viewBinding.tvScreenNoLimit.setOnClickListener {
            viewBinding.etScreenMaxNum.clearFocus()
            updateScreenLimit(PlayerInitializer.Danmu.DEFAULT_MAX_NUM)
        }

        viewBinding.tvScreenAutoLimit.setOnClickListener {
            viewBinding.etScreenMaxNum.clearFocus()
            updateScreenLimit(-1)
        }

        viewBinding.etScreenMaxNum.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //输入为空，设置为无限制
                val maxNumText = viewBinding.etScreenMaxNum.text.toString()
                val newMaxNum = maxNumText.toIntOrNull() ?: PlayerInitializer.Danmu.DEFAULT_MAX_NUM

                updateScreenLimit(newMaxNum)
                hideKeyboard(viewBinding.etScreenMaxNum)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun applyDanmuConfigureStatus() {
        when (settingMode) {
            BaseDanmaku.TYPE_SCROLL_RL -> applyScrollDanmuConfigure()
            BaseDanmaku.TYPE_FIX_TOP -> applyTopDanmuConfigure()
            BaseDanmaku.TYPE_FIX_BOTTOM -> applyBottomDanmuConfigure()
        }
    }

    private fun applyScrollDanmuConfigure() {
        viewBinding.tvLineLimitTips.text = "滚动弹幕行数限制"
        viewBinding.tvDanmuEnableTips.text = "启用滚动弹幕"
        viewBinding.groupScreenLimit.isVisible = true
        viewBinding.switchDanmuEnable.isChecked = PlayerInitializer.Danmu.mobileDanmu
        if (PlayerInitializer.Danmu.maxScrollLine == PlayerInitializer.Danmu.DEFAULT_MAX_LINE) {
            viewBinding.tvLineNoLimit.isSelected = true
            viewBinding.etMaxLine.setText("")
        } else {
            viewBinding.tvLineNoLimit.isSelected = false
            viewBinding.etMaxLine.setText(PlayerInitializer.Danmu.maxScrollLine.toString())
        }

        when (PlayerInitializer.Danmu.maxNum) {
            PlayerInitializer.Danmu.DEFAULT_MAX_NUM -> {
                viewBinding.tvScreenNoLimit.isSelected = true
                viewBinding.tvScreenAutoLimit.isSelected = false
                viewBinding.etScreenMaxNum.setText("")
            }
            -1 -> {
                viewBinding.tvScreenNoLimit.isSelected = false
                viewBinding.tvScreenAutoLimit.isSelected = true
                viewBinding.etScreenMaxNum.setText("")
            }
            else -> {
                viewBinding.tvScreenNoLimit.isSelected = false
                viewBinding.tvScreenAutoLimit.isSelected = false
                viewBinding.etScreenMaxNum.setText(PlayerInitializer.Danmu.maxNum.toString())
            }
        }
    }

    private fun applyTopDanmuConfigure() {
        viewBinding.tvLineLimitTips.text = "顶部弹幕行数限制"
        viewBinding.tvDanmuEnableTips.text = "启用顶部弹幕"
        viewBinding.groupScreenLimit.isVisible = false
        viewBinding.switchDanmuEnable.isChecked = PlayerInitializer.Danmu.topDanmu
        if (PlayerInitializer.Danmu.maxTopLine == PlayerInitializer.Danmu.DEFAULT_MAX_LINE) {
            viewBinding.tvLineNoLimit.isSelected = true
            viewBinding.etMaxLine.setText("")
        } else {
            viewBinding.tvLineNoLimit.isSelected = false
            viewBinding.etMaxLine.setText(PlayerInitializer.Danmu.maxTopLine.toString())
        }
    }

    private fun applyBottomDanmuConfigure() {
        viewBinding.tvLineLimitTips.text = "底部弹幕行数限制"
        viewBinding.tvDanmuEnableTips.text = "启用底部弹幕"
        viewBinding.groupScreenLimit.isVisible = false
        viewBinding.switchDanmuEnable.isChecked = PlayerInitializer.Danmu.bottomDanmu
        if (PlayerInitializer.Danmu.maxBottomLine == PlayerInitializer.Danmu.DEFAULT_MAX_LINE) {
            viewBinding.tvLineNoLimit.isSelected = true
            viewBinding.etMaxLine.setText("")
        } else {
            viewBinding.tvLineNoLimit.isSelected = false
            viewBinding.etMaxLine.setText(PlayerInitializer.Danmu.maxBottomLine.toString())
        }
    }

    private fun updateDanmuEnable(enable: Boolean) {
        when (settingMode) {
            BaseDanmaku.TYPE_SCROLL_RL -> {
                PlayerInitializer.Danmu.mobileDanmu = enable
                DanmuConfig.putShowMobileDanmu(enable)
                mControlWrapper.updateMobileDanmuState()
            }
            BaseDanmaku.TYPE_FIX_TOP -> {
                PlayerInitializer.Danmu.topDanmu = enable
                DanmuConfig.putShowTopDanmu(enable)
                mControlWrapper.updateTopDanmuState()
            }
            BaseDanmaku.TYPE_FIX_BOTTOM -> {
                PlayerInitializer.Danmu.bottomDanmu = enable
                DanmuConfig.putShowBottomDanmu(enable)
                mControlWrapper.updateBottomDanmuState()
            }
        }
    }

    private fun updateMaxLine(line: Int) {
        when (settingMode) {
            BaseDanmaku.TYPE_SCROLL_RL -> {
                PlayerInitializer.Danmu.maxScrollLine = line
                DanmuConfig.putDanmuScrollMaxLine(line)
            }
            BaseDanmaku.TYPE_FIX_TOP -> {
                PlayerInitializer.Danmu.maxTopLine = line
                DanmuConfig.putDanmuTopMaxLine(line)
            }
            BaseDanmaku.TYPE_FIX_BOTTOM -> {
                PlayerInitializer.Danmu.maxBottomLine = line
                DanmuConfig.putDanmuBottomMaxLine(line)
            }
        }
        mControlWrapper.updateMaxLine()
        applyDanmuConfigureStatus()
    }

    private fun updateScreenLimit(limit: Int) {
        PlayerInitializer.Danmu.maxNum = limit
        DanmuConfig.putDanmuMaxCount(limit)
        mControlWrapper.updateMaxScreenNum()
        applyDanmuConfigureStatus()
    }

    private fun handleKeyCode(keyCode: Int) {
        if (viewBinding.tvKeywordBlock.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (settingMode == BaseDanmaku.TYPE_SCROLL_RL) {
                        viewBinding.tvScreenNoLimit.requestFocus()
                    } else {
                        viewBinding.tvLineNoLimit.requestFocus()
                    }
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    when (settingMode) {
                        BaseDanmaku.TYPE_SCROLL_RL -> viewBinding.llScrollDanmu.requestFocus()
                        BaseDanmaku.TYPE_FIX_TOP -> viewBinding.llTopDanmu.requestFocus()
                        BaseDanmaku.TYPE_FIX_BOTTOM -> viewBinding.llBottomDanmu.requestFocus()
                    }
                }
            }
        } else if (viewBinding.llScrollDanmu.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.tvKeywordBlock.requestFocus()
                KeyEvent.KEYCODE_DPAD_LEFT -> viewBinding.llTopDanmu.requestFocus()
                KeyEvent.KEYCODE_DPAD_RIGHT -> viewBinding.llBottomDanmu.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.switchDanmuEnable.requestFocus()
            }
        } else if (viewBinding.llBottomDanmu.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.tvKeywordBlock.requestFocus()
                KeyEvent.KEYCODE_DPAD_LEFT -> viewBinding.llScrollDanmu.requestFocus()
                KeyEvent.KEYCODE_DPAD_RIGHT -> viewBinding.llTopDanmu.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.switchDanmuEnable.requestFocus()
            }
        } else if (viewBinding.llTopDanmu.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.tvKeywordBlock.requestFocus()
                KeyEvent.KEYCODE_DPAD_LEFT -> viewBinding.llBottomDanmu.requestFocus()
                KeyEvent.KEYCODE_DPAD_RIGHT -> viewBinding.llScrollDanmu.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.switchDanmuEnable.requestFocus()
            }
        } else if (viewBinding.switchDanmuEnable.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    when (settingMode) {
                        BaseDanmaku.TYPE_SCROLL_RL -> viewBinding.llScrollDanmu.requestFocus()
                        BaseDanmaku.TYPE_FIX_TOP -> viewBinding.llTopDanmu.requestFocus()
                        BaseDanmaku.TYPE_FIX_BOTTOM -> viewBinding.llBottomDanmu.requestFocus()
                    }
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.tvLineNoLimit.requestFocus()
            }
        } else if (viewBinding.tvLineNoLimit.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.switchDanmuEnable.requestFocus()
                KeyEvent.KEYCODE_DPAD_RIGHT -> viewBinding.etMaxLine.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (settingMode == BaseDanmaku.TYPE_SCROLL_RL) {
                        viewBinding.tvScreenNoLimit.requestFocus()
                    } else {
                        viewBinding.tvKeywordBlock.requestFocus()
                    }
                }
            }
        } else if (viewBinding.etMaxLine.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.tvLineNoLimit.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (settingMode == BaseDanmaku.TYPE_SCROLL_RL) {
                        viewBinding.tvScreenNoLimit.requestFocus()
                    } else {
                        viewBinding.tvKeywordBlock.requestFocus()
                    }
                }
            }
        } else if (viewBinding.tvScreenNoLimit.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.tvLineNoLimit.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.tvKeywordBlock.requestFocus()
                KeyEvent.KEYCODE_DPAD_RIGHT -> viewBinding.tvScreenAutoLimit.requestFocus()
            }
        } else if (viewBinding.tvScreenAutoLimit.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.tvLineNoLimit.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.tvKeywordBlock.requestFocus()
                KeyEvent.KEYCODE_DPAD_LEFT -> viewBinding.tvScreenNoLimit.requestFocus()
                KeyEvent.KEYCODE_DPAD_RIGHT -> viewBinding.etScreenMaxNum.requestFocus()
            }
        } else if (viewBinding.etScreenMaxNum.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> viewBinding.tvScreenAutoLimit.requestFocus()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewBinding.tvKeywordBlock.requestFocus()
            }
        } else {
            viewBinding.llScrollDanmu.requestFocus()
        }
    }
}