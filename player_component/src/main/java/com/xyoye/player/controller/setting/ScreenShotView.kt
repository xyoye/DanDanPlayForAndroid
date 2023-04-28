package com.xyoye.player.controller.setting

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.common_component.utils.getScreenHeight
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.PlayerType
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.data_component.enums.SurfaceType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.LayoutSceenShotBinding
import com.xyoye.player_component.utils.PlayRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by xyoye on 2021/5/2.
 */

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

    private var mGenerateImageJob: Job? = null

    init {
        viewBinding.screenShotLayout.translationY = -mAttachActivity.getScreenHeight().toFloat()

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
            prepareScreenShot()
            ViewCompat.animate(viewBinding.screenShotLayout).translationY(0f).setDuration(300)
                .start()
        } else {
            mGenerateImageJob?.cancel()
            val hideY = -mAttachActivity.getScreenHeight().toFloat()
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

    override fun onPopupModeChanged(isPopup: Boolean) {

    }

    private fun prepareScreenShot() {
        mGenerateImageJob?.cancel()
        mGenerateImageJob = mAttachActivity.lifecycleScope.launch(Dispatchers.Main) {
            val videoSize = controlWrapper.getVideoSize()
            fixVlcTextureSize(videoSize)
            bitmap = withContext(Dispatchers.IO) {
                generateVideoImage(videoSize)
            }
            if (bitmap == null) {
                ToastCenter.showOriginalToast("获取截图失败")
                onSettingVisibilityChanged(false)
                return@launch
            }

            controlWrapper.pause()
            controlWrapper.hideController()

            viewBinding.shotIv.setImageBitmap(bitmap)
        }
    }

    /**
     *  VLC TextureView 通过视频尺寸获取到的截图会扭曲，需要通过View尺寸获取截图
     */
    private fun fixVlcTextureSize(videoSize: Point) {
        if (PlayerInitializer.playerType != PlayerType.TYPE_VLC_PLAYER) {
            return
        }
        if (PlayerInitializer.surfaceType != SurfaceType.VIEW_TEXTURE) {
            return
        }
        controlWrapper.getRenderView()?.getView()?.let {
            videoSize.x = it.width
            videoSize.y = it.height
        }
    }

    private suspend fun generateVideoImage(videoSize: Point): Bitmap? {
        val renderView = controlWrapper.getRenderView()?.getView()
            ?: return null
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            return null
        }
        return PlayRecorder.generateRenderImage(renderView, videoSize)
    }
}