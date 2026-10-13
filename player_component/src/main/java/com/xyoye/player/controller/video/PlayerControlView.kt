package com.xyoye.player.controller.video

import android.content.Context
import android.graphics.Point
import android.view.LayoutInflater
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.xyoye.common_component.utils.dp2px
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.utils.MessageTime
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutPlayerControllerBinding

/**
 * Created by xyoye on 2022/11/13.
 */

class PlayerControlView(context: Context): InterControllerView {

    private val viewBinding = DataBindingUtil.inflate<LayoutPlayerControllerBinding>(
        LayoutInflater.from(context),
        R.layout.layout_player_controller,
        null,
        false
    )

    private lateinit var mControlWrapper: ControlWrapper

    init {
        viewBinding.playerLockIv.setOnClickListener {
            mControlWrapper.toggleLockState()
        }
        viewBinding.playerShotIv.setOnClickListener {
            mControlWrapper.showSettingView(SettingViewType.SCREEN_SHOT)
        }
        // 复位会触发 onZoomStateChanged，按钮随之收起
        viewBinding.playerRestoreTv.setOnClickListener {
            mControlWrapper.resetZoom(true)
        }
    }

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView() = viewBinding.root

    override fun onVisibilityChanged(isVisible: Boolean) {
        updateLockVisible(isVisible)
        if (mControlWrapper.isLocked().not()) {
            updateShotVisible(isVisible)
        }
        refreshZoomRestore()
    }

    override fun onPlayStateChanged(playState: PlayState) {

    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {
        viewBinding.playerLockIv.isSelected = isLocked
        updateShotVisible(!isLocked)
        refreshZoomRestore()
    }

    /**
     * 刷新「还原屏幕」按钮显隐
     *
     * 与进度栏（控制栏）同步显隐：仅当控制栏显示、未锁定且画面处于缩放状态时显示
     */
    fun refreshZoomRestore() {
        viewBinding.playerRestoreTv.isVisible =
            mControlWrapper.isControllerShowing() &&
                    mControlWrapper.isLocked().not() &&
                    mControlWrapper.isZoomed()
    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }

    override fun onPopupModeChanged(isPopup: Boolean) {

    }

    fun showMessage(text: String, time: MessageTime) {
        viewBinding.messageContainer.showMessage(text, time)
    }

    fun clearMessage() {
        viewBinding.messageContainer.clearMessage()
    }

    private fun updateLockVisible(isVisible: Boolean) {
        if (isVisible) {
            viewBinding.playerLockIv.isVisible = true
            ViewCompat.animate(viewBinding.playerLockIv)
                .translationX(0f)
                .setDuration(300)
                .start()
        } else {
            val translateX = dp2px(60).toFloat()
            ViewCompat.animate(viewBinding.playerLockIv)
                .translationX(-translateX)
                .setDuration(300)
                .start()
        }
    }

    private fun updateShotVisible(isVisible: Boolean) {
        if (isVisible) {
            viewBinding.playerShotIv.isVisible = true
            ViewCompat.animate(viewBinding.playerShotIv)
                .translationX(0f)
                .setDuration(300)
                .start()
        } else {
            val translateX = dp2px(60).toFloat()
            ViewCompat.animate(viewBinding.playerShotIv)
                .translationX(translateX)
                .setDuration(300)
                .start()
        }
    }
}