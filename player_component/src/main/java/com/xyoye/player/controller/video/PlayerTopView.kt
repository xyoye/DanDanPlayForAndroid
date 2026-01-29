package com.xyoye.player.controller.video

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import com.xyoye.common_component.extension.toText
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutPlayerTopBinding
import com.xyoye.player_component.ui.activities.overlay_permission.OverlayPermissionActivity
import com.xyoye.player_component.utils.BatteryHelper
import java.util.Date

/**
 * Created by xyoye on 2020/11/3.
 */

class PlayerTopView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), InterControllerView {

    private val viewBinding = DataBindingUtil.inflate<LayoutPlayerTopBinding>(
        LayoutInflater.from(context),
        R.layout.layout_player_top,
        this,
        true
    )

    private var exitPlayerObserver: (() -> Unit)? = null

    private var enterPopupModeBlock: (() -> Unit)? = null

    private lateinit var mControlWrapper: ControlWrapper

    init {
        // 新增焦点占位视图，避免焦点默认落在返回按钮上
        viewBinding.focusPlaceholder.setOnClickListener {
            mControlWrapper.togglePlay()
        }

        viewBinding.backIv.setOnClickListener {
            exitPlayerObserver?.invoke()
        }

        viewBinding.playerSettingsIv.setOnClickListener {
            mControlWrapper.showSettingView(SettingViewType.PLAYER_SETTING)
        }

        viewBinding.ivSwitchPopup.setOnClickListener {
            if (OverlayPermissionActivity.hasOverlayPermission().not()) {
                OverlayPermissionActivity.requestOverlayPermission(context)
                return@setOnClickListener
            }
            enterPopupModeBlock?.invoke()
        }

        // 将初始焦点置于标题，而不是返回按钮
        post { viewBinding.videoTitleTv.requestFocus() }
    }

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView() = this

    override fun onVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            //不加延迟会导致动画卡顿
            postDelayed({
                viewBinding.systemTimeTv.text = Date().toText("HH:mm")
            }, 100)

            ViewCompat.animate(viewBinding.playerTopLl).translationY(0f).setDuration(300).start()
        } else {
            viewBinding.videoTitleTv.requestFocus()
            val height = viewBinding.playerTopLl.height.toFloat()
            ViewCompat.animate(viewBinding.playerTopLl).translationY(-height)
                .setDuration(300).start()
        }
    }

    override fun onPlayStateChanged(playState: PlayState) {

    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {
        //显示状态与锁定状态相反
        onVisibilityChanged(!isLocked)
    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }

    override fun onPopupModeChanged(isPopup: Boolean) {

    }

    fun setBatteryHelper(helper: BatteryHelper) {
        helper.bindBatteryView(viewBinding.batteryView)
    }

    fun setVideoTitle(title: String?) {
        viewBinding.videoTitleTv.text = title
    }

    fun setExitPlayerObserver(block: () -> Unit) {
        exitPlayerObserver = block
    }

    fun setEnterPopupModeObserver(block: () -> Unit) {
        enterPopupModeBlock = block
    }
}