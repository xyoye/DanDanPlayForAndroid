package com.xyoye.player.controller.setting

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.core.view.isVisible
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.extension.toResString
import com.xyoye.data_component.enums.LoadDanmuState
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSettingDanmuBinding

/**
 * Created by xyoye on 2020/11/20.
 */

class SettingDanmuView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingDanmuBinding>(context, attrs, defStyleAttr) {

    init {
        initSettingListener()
    }

    override fun getLayoutId() = R.layout.layout_setting_danmu

    override fun getSettingViewType() = SettingViewType.DANMU_SETTING

    override fun onSettingVisibilityChanged(isVisible: Boolean) {
        super.onSettingVisibilityChanged(isVisible)
        if (isVisible) {
            updateSettingView()
        }
    }

    fun onDanmuSourceChanged() {
        val danmuPath = mControlWrapper.getDanmuUrl()
        if (TextUtils.isEmpty(danmuPath)) {
            viewBinding.tvDanmuPath.text = R.string.not_loaded.toResString()
            viewBinding.tvDanmuPath.setTextColor(R.color.text_red.toResColor())
            viewBinding.tvRemoveDanmuSource.isVisible = false
            return
        }

        val path = danmuPath!!.replaceFirst("/storage/emulate/0", "根目录")
        viewBinding.tvDanmuPath.text = path
        viewBinding.tvDanmuPath.setTextColor(R.color.text_gray.toResColor())
        viewBinding.tvRemoveDanmuSource.isVisible = true
    }

    fun updateLoadDanmuSate(state: LoadDanmuState) {
        viewBinding.tvDanmuPath.text = state.msg
        viewBinding.tvDanmuPath.setTextColor(R.color.text_gray.toResColor())
        viewBinding.tvRemoveDanmuSource.isVisible = false
    }

    private fun initSettingListener() {
        viewBinding.tvRemoveDanmuSource.setOnClickListener {
            mControlWrapper.onDanmuSourceChanged("")
        }

        viewBinding.tvSwitchLocalDanmu.setOnClickListener {
            mControlWrapper.switchSource(false)
            onSettingVisibilityChanged(false)
        }

        viewBinding.tvSearchNetworkDanmu.setOnClickListener {
            onSettingVisibilityChanged(false)
        }

        viewBinding.layoutDanmuConfig.setOnClickListener {
            mControlWrapper.showSettingView(SettingViewType.DANMU_SETTING_CONFIG)
            onSettingVisibilityChanged(false)
        }

        viewBinding.layoutDanmuBlockSetting.setOnClickListener {
            mControlWrapper.showSettingView(SettingViewType.DANMU_SETTING_BLOCK)
            onSettingVisibilityChanged(false)
        }
    }

    private fun updateSettingView() {
        viewBinding.tvDanmuSettingDesc.text = getSettingParams()

        viewBinding.tvDanmuBlockDesc.text = getBlockSetting()
    }

    private fun getSettingParams(): String {
        val params = StringBuilder()
        val danmuSizePercent = PlayerInitializer.Danmu.size
        params.append("[大小：").append(danmuSizePercent).append("%]").append("    ")

        val danmuSpeedPercent = PlayerInitializer.Danmu.speed
        params.append("[速度：").append(danmuSpeedPercent).append("%]").append("    ")

        if (PlayerInitializer.Danmu.offsetPosition != 0L) {
            val extraPosition = PlayerInitializer.Danmu.offsetPosition / 1000f
            val extraPositionStr = if (extraPosition > 0) "+$extraPosition" else "-$extraPosition"
            params.append("[时间：").append(extraPositionStr).append("]")
        }
        return params.toString().trim()
    }

    private fun getBlockSetting(): String {
        val setting = StringBuilder()
        if (PlayerInitializer.Danmu.mobileDanmu.not()) {
            setting.append("[滚动]").append("  ")
        }
        if (PlayerInitializer.Danmu.topDanmu.not()) {
            setting.append("[顶部]").append("  ")
        }
        if (PlayerInitializer.Danmu.bottomDanmu.not()) {
            setting.append("[底部]").append("  ")
        }
        if (PlayerInitializer.Danmu.maxLine == PlayerInitializer.Danmu.DEFAULT_MAX_LINE) {
            setting.append("[行数：∞]").append("  ")
        } else {
            setting.append("[行数：").append(PlayerInitializer.Danmu.maxLine).append("]  ")
        }
        when (PlayerInitializer.Danmu.maxNum) {
            PlayerInitializer.Danmu.DEFAULT_MAX_NUM -> {
                setting.append("[数量：∞]")
            }
            -1 -> {
                setting.append("[数量：自动]")
            }
            else -> {
                setting.append("[数量：").append(PlayerInitializer.Danmu.maxNum).append("]")
            }
        }

        return setting.toString().trim()
    }
}