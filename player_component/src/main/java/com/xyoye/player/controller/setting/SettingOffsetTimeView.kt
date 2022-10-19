package com.xyoye.player.controller.setting

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.core.view.isVisible
import com.xyoye.common_component.utils.dp2px
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSettingOffsetTimeBinding
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/10/18
 *     desc  :
 * </pre>
 */

class SettingOffsetTimeView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSettingOffsetTimeBinding>(context, attrs, defStyleAttr) {

    private val layoutHeight = dp2px(100).toFloat()
    private val actionViews = mutableListOf<TextView>()
    private var mSettingType = SettingViewType.DANMU_OFFSET_TIME

    init {
        initView()
    }

    override fun getLayoutId() = R.layout.layout_setting_offset_time

    override fun getSettingViewType() = SettingViewType.DANMU_OFFSET_TIME

    override fun onSettingVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            ViewCompat.animate(viewBinding.root)
                .translationY(0f)
                .setDuration(500)
                .setListener(object : ViewPropertyAnimatorListener {
                    override fun onAnimationStart(view: View) {
                        onViewShow()
                    }

                    override fun onAnimationEnd(view: View) {
                        onViewShowed()
                    }

                    override fun onAnimationCancel(view: View) {
                        onViewHide()
                    }
                })
                .start()
        } else {
            ViewCompat.animate(viewBinding.root)
                .translationY(layoutHeight)
                .setDuration(500)
                .setListener(object : ViewPropertyAnimatorListener {
                    override fun onAnimationStart(view: View) {
                        onViewHide()
                    }

                    override fun onAnimationEnd(view: View) {

                    }

                    override fun onAnimationCancel(view: View) {

                    }
                })
                .start()
        }
    }

    override fun isSettingShowing(): Boolean {
        return viewBinding.root.translationY == 0f
    }

    override fun onViewShow() {
        changeTime(0f, dispatch = false)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isSettingShowing().not()) {
            return false
        }

        handleKeyCode(keyCode)
        return true
    }

    private fun initView() {
        actionViews.clear()
        actionViews.add(viewBinding.tvReduceTen)
        actionViews.add(viewBinding.tvReduceFive)
        actionViews.add(viewBinding.tvReduceHalf)
        actionViews.add(viewBinding.tvReset)
        actionViews.add(viewBinding.tvAddHalf)
        actionViews.add(viewBinding.tvAddFive)
        actionViews.add(viewBinding.tvAddTen)

        actionViews.forEach { textView ->
            textView.setOnClickListener {
                if (textView == viewBinding.tvReset) {
                    changeTime(0f, reset = true)
                    return@setOnClickListener
                }

                val time = textView.text.toString().toFloatOrNull()
                    ?: return@setOnClickListener
                changeTime(time)
            }
        }
    }

    private fun changeTime(time: Float, reset: Boolean = false, dispatch: Boolean = true) {
        val offsetTime = if (mSettingType == SettingViewType.SUBTITLE_OFFSET_TIME) {
            PlayerInitializer.Subtitle.offsetPosition
        } else {
            PlayerInitializer.Danmu.offsetPosition
        }
        val currentOffset = time * 1000
        var newOffset = offsetTime + currentOffset

        if (reset) {
            newOffset = 0f
        }

        if (dispatch) {
            if (mSettingType == SettingViewType.SUBTITLE_OFFSET_TIME) {
                PlayerInitializer.Subtitle.offsetPosition = newOffset.toLong()
                mControlWrapper.updateSubtitleOffsetTime()
            } else {
                PlayerInitializer.Danmu.offsetPosition = newOffset.toLong()
                mControlWrapper.updateDanmuOffsetTime()
            }
        }

        val display = "${numberFormat(abs(newOffset / 1000))}秒"
        val status = when {
            newOffset > 0 -> "提前"
            newOffset < 0 -> "延迟"
            else -> ""
        }

        viewBinding.tvDisplay.text = display
        viewBinding.tvStatus.text = status
        viewBinding.tvReset.isVisible = newOffset != 0f
    }

    private fun handleKeyCode(keyCode: Int) {
        val focusedChild = viewBinding.settingLayout.focusedChild
        if (focusedChild == null) {
            actionViews.first().requestFocus()
            return
        }

        val focusedIndex = actionViews.indexOf(focusedChild)
        if (focusedIndex == -1) {
            actionViews.first().requestFocus()
            return
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            val targetView = actionViews.getOrNull(focusedIndex - 1) ?: actionViews.last()
            targetView.requestFocus()
            return
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            val targetView = actionViews.getOrNull(focusedIndex + 1) ?: actionViews.first()
            targetView.requestFocus()
            return
        }
    }

    private fun numberFormat(num: Float): String {
        return DecimalFormat("0.#").run {
            roundingMode = RoundingMode.HALF_UP
            format(num)
        }
    }

    fun setSettingType(settingType: SettingViewType) {
        this.mSettingType = settingType
    }
}