package com.xyoye.player.controller.setting

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.common_component.utils.getScreenHeight
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSceenShotBinding

/**
 * Created by xyoye on 2021/5/2.
 */

@SuppressLint("ClickableViewAccessibility")
class ScreenShotView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), InterSettingView {
    private lateinit var controlWrapper: ControlWrapper
    private var bitmap: Bitmap? = null
    private val mAttachActivity = context as AppCompatActivity

    private val viewBinding = DataBindingUtil.inflate<LayoutSceenShotBinding>(
        LayoutInflater.from(context),
        R.layout.layout_sceen_shot,
        this,
        true
    )

    init {
        post {
            val hideY = -((mAttachActivity.getScreenHeight() - height) / 2f + height.toFloat())
            viewBinding.screenShotLayout.translationY = hideY
        }

        viewBinding.screenShotLayout.setOnTouchListener { _, _ ->
            return@setOnTouchListener true
        }

        viewBinding.shotCancelBt.setOnClickListener {
            onSettingVisibilityChanged(false)
        }

        viewBinding.shotSaveBt.setOnClickListener {
            if (bitmap == null) {
                onSettingVisibilityChanged(false)
                return@setOnClickListener
            }
            val (isSuccess, dirType) = MediaUtils.saveScreenShot(context, bitmap!!)
            if (isSuccess) {
                ToastCenter.showOriginalToast("$dirType: 保存截图成功")
            } else {
                ToastCenter.showOriginalToast("保存截图失败")
            }
            onSettingVisibilityChanged(false)
        }
    }

    override fun getSettingViewType() = SettingViewType.SCREEN_SHOT

    override fun onSettingVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            ViewCompat.animate(viewBinding.screenShotLayout).translationY(0f).setDuration(300)
                .start()

            val shotBitmap = controlWrapper.doScreenShot()
            if (shotBitmap == null) {
                ToastCenter.showOriginalToast("当前渲染器不支持截屏")
                onSettingVisibilityChanged(false)
                return
            }
            bitmap = shotBitmap
            controlWrapper.pause()
            controlWrapper.hideController()

            viewBinding.shotIv.setImageBitmap(shotBitmap)
        } else {
            val hideY = -((mAttachActivity.getScreenHeight() - height) / 2f + height.toFloat())
            ViewCompat.animate(viewBinding.screenShotLayout).translationY(hideY).setDuration(300)
                .start()
        }
    }

    override fun isSettingShowing() = viewBinding.screenShotLayout.translationY == 0f

    override fun attach(controlWrapper: ControlWrapper) {
        this.controlWrapper = controlWrapper
    }

    override fun getView() = this

    override fun onVisibilityChanged(isVisible: Boolean) {

    }

    override fun onPlayStateChanged(playState: PlayState) {

    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {

    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }
}