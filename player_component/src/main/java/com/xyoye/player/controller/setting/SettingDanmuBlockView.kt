package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.utils.hideKeyboard
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSettingDanmuBlockBinding


/**
 * Created by xyoye on 2022/1/10
 */

class SettingDanmuBlockView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingDanmuBlockBinding>(context, attrs, defStyleAttr) {

    init {
        initSettingView()

        initSettingListener()
    }

    override fun getLayoutId() = R.layout.layout_setting_danmu_block

    override fun getSettingViewType() = SettingViewType.DANMU_SETTING_BLOCK

    private fun initSettingView() {
        //弹幕类型屏蔽
        viewBinding.mobileDanmuIv.isSelected = !PlayerInitializer.Danmu.mobileDanmu
        viewBinding.topDanmuIv.isSelected = !PlayerInitializer.Danmu.topDanmu
        viewBinding.bottomDanmuIv.isSelected = !PlayerInitializer.Danmu.bottomDanmu

        //滚动弹幕行数限制
        updateMaxDanmuLine()

        //弹幕同屏数量限制
        updateMaxDanmuNum()

        viewBinding.tvResetDanmuConfig.isVisible = isConfigChanged()
    }

    private fun initSettingListener() {
        viewBinding.tvResetDanmuConfig.setOnClickListener {
            resetConfig()
        }

        viewBinding.keywordBlockTv.setOnClickListener {
            onSettingVisibilityChanged(false)
            mControlWrapper.showSettingView(SettingViewType.KEYWORD_BLOCK)
        }

        viewBinding.mobileDanmuIv.setOnClickListener {
            updateMobile(!PlayerInitializer.Danmu.mobileDanmu)
        }

        viewBinding.topDanmuIv.setOnClickListener {
            updateTop(!PlayerInitializer.Danmu.topDanmu)
        }

        viewBinding.bottomDanmuIv.setOnClickListener {
            updateBottom(!PlayerInitializer.Danmu.bottomDanmu)
        }

        viewBinding.maxLineTv.setOnClickListener {
            updateMaxLine(PlayerInitializer.Danmu.DEFAULT_MAX_LINE)
        }

        viewBinding.maxLineEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //输入为空或0，设置为无限制
                val maxLineText = viewBinding.maxLineEt.text.toString()
                var newMaxLine = maxLineText.toIntOrNull() ?: PlayerInitializer.Danmu.DEFAULT_MAX_LINE
                newMaxLine = if (newMaxLine == 0) PlayerInitializer.Danmu.DEFAULT_MAX_LINE else newMaxLine

                updateMaxLine(newMaxLine)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        viewBinding.numberNoLimitTv.setOnClickListener {
            updateMaxNum(PlayerInitializer.Danmu.DEFAULT_MAX_NUM)
        }

        viewBinding.numberAutoLimitTv.setOnClickListener {
            updateMaxNum(-1)
        }

        viewBinding.numberInputLimitEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //输入为空，设置为无限制
                val maxNumText = viewBinding.numberInputLimitEt.text.toString()
                val newMaxNum = maxNumText.toIntOrNull() ?: PlayerInitializer.Danmu.DEFAULT_MAX_NUM

                updateMaxNum(newMaxNum)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun updateMobile(enable: Boolean) {
        if (PlayerInitializer.Danmu.mobileDanmu == enable)
            return

        viewBinding.mobileDanmuIv.isSelected = !enable
        PlayerInitializer.Danmu.mobileDanmu = enable
        DanmuConfig.putShowMobileDanmu(enable)
        mControlWrapper.updateMobileDanmuState()
        onConfigChanged()
    }

    private fun updateTop(enable: Boolean) {
        if (PlayerInitializer.Danmu.topDanmu == enable)
            return

        viewBinding.topDanmuIv.isSelected = !enable
        PlayerInitializer.Danmu.topDanmu = enable
        DanmuConfig.putShowTopDanmu(enable)
        mControlWrapper.updateTopDanmuState()
        onConfigChanged()
    }

    private fun updateBottom(enable: Boolean) {
        if (PlayerInitializer.Danmu.bottomDanmu == enable)
            return
        viewBinding.bottomDanmuIv.isSelected = !enable
        PlayerInitializer.Danmu.bottomDanmu = enable
        DanmuConfig.putShowBottomDanmu(enable)
        mControlWrapper.updateBottomDanmuState()
        onConfigChanged()
    }

    private fun updateMaxLine(line: Int) {
        if (PlayerInitializer.Danmu.maxLine == line)
            return
        hideKeyboard(viewBinding.maxLineEt)
        viewBinding.maxLineLl.requestFocus()
        PlayerInitializer.Danmu.maxLine = line
        DanmuConfig.putDanmuMaxLine(line)
        updateMaxDanmuLine()
        mControlWrapper.updateMaxLine()
        onConfigChanged()
    }

    private fun updateMaxNum(num: Int) {
        if (PlayerInitializer.Danmu.maxNum == num)
            return
        viewBinding.numberLimitRl.requestFocus()
        hideKeyboard(viewBinding.numberInputLimitEt)
        PlayerInitializer.Danmu.maxNum = num
        DanmuConfig.putDanmuMaxCount(num)
        updateMaxDanmuNum()
        mControlWrapper.updateMaxScreenNum()
        onConfigChanged()
    }

    private fun updateMaxDanmuLine() {
        if (PlayerInitializer.Danmu.maxLine == -1) {
            viewBinding.maxLineTv.isSelected = true
            viewBinding.maxLineEt.setText("")
        } else {
            viewBinding.maxLineTv.isSelected = false
            viewBinding.maxLineEt.setText(PlayerInitializer.Danmu.maxLine.toString())
        }
    }

    private fun updateMaxDanmuNum() {
        when (PlayerInitializer.Danmu.maxNum) {
            0 -> {
                viewBinding.numberNoLimitTv.isSelected = true
                viewBinding.numberAutoLimitTv.isSelected = false
                viewBinding.numberInputLimitEt.setText("")
            }
            -1 -> {
                viewBinding.numberNoLimitTv.isSelected = false
                viewBinding.numberAutoLimitTv.isSelected = true
                viewBinding.numberInputLimitEt.setText("")
            }
            else -> {
                viewBinding.numberNoLimitTv.isSelected = false
                viewBinding.numberAutoLimitTv.isSelected = false
                viewBinding.numberInputLimitEt.setText(PlayerInitializer.Danmu.maxNum.toString())
            }
        }
    }

    private fun resetConfig() {
        updateMobile(PlayerInitializer.Danmu.DEFAULT_MOBILE_ENABLE)
        updateTop(PlayerInitializer.Danmu.DEFAULT_TOP_ENABLE)
        updateBottom(PlayerInitializer.Danmu.DEFAULT_BOTTOM_ENABLE)
        updateMaxLine(PlayerInitializer.Danmu.DEFAULT_MAX_LINE)
        updateMaxNum(PlayerInitializer.Danmu.DEFAULT_MAX_NUM)
    }

    private fun onConfigChanged() {
        viewBinding.tvResetDanmuConfig.isVisible = isConfigChanged()
    }

    private fun isConfigChanged(): Boolean {
        return PlayerInitializer.Danmu.mobileDanmu != PlayerInitializer.Danmu.DEFAULT_MOBILE_ENABLE
                || PlayerInitializer.Danmu.topDanmu != PlayerInitializer.Danmu.DEFAULT_TOP_ENABLE
                || PlayerInitializer.Danmu.bottomDanmu != PlayerInitializer.Danmu.DEFAULT_BOTTOM_ENABLE
                || PlayerInitializer.Danmu.maxLine != PlayerInitializer.Danmu.DEFAULT_MAX_LINE
                || PlayerInitializer.Danmu.maxNum != PlayerInitializer.Danmu.DEFAULT_MAX_NUM
    }
}