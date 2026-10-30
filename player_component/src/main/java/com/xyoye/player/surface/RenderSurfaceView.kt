package com.xyoye.player.surface

import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.kernel.inter.AbstractVideoPlayer
import com.xyoye.player.utils.RenderMeasureHelper

/**
 * Created by xyoye on 2020/11/3.
 */

class RenderSurfaceView(context: Context) : SurfaceView(context), InterSurfaceView {

    private val mMeasureHelper = RenderMeasureHelper()
    private lateinit var mVideoPlayer: AbstractVideoPlayer

    // Surface 生命周期回调是否已注册
    private var isSurfaceCallbackRegistered = false

    // 渲染视图是否已释放
    private var isReleased = false

    private val mSurfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // 销毁时解除播放器输出，避免解码器继续向已废弃的 BufferQueue 写入画面
            if (this@RenderSurfaceView::mVideoPlayer.isInitialized) {
                mVideoPlayer.clearSurface(holder.surface)
            }
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            // Surface 创建后重新绑定播放器输出
            if (this@RenderSurfaceView::mVideoPlayer.isInitialized) {
                mVideoPlayer.setSurface(holder.surface)
            }
        }
    }

    init {
        registerSurfaceCallback()
    }

    override fun attachPlayer(player: AbstractVideoPlayer) {
        mVideoPlayer = player
    }

    override fun setVideoSize(videoWidth: Int, videoHeight: Int) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.mVideoWidth = videoWidth
            mMeasureHelper.mVideoHeight = videoHeight
            requestLayout()
        }
    }

    override fun setVideoRotation(degree: Int) {
        mMeasureHelper.mVideoDegree = degree
        if (rotation != degree.toFloat()) {
            rotation = degree.toFloat()
            requestLayout()
        }
    }

    override fun setScaleType(screenScale: VideoScreenScale) {
        mMeasureHelper.mScreenScale = screenScale
        requestLayout()
    }

    override fun getView() = this

    override fun refresh() {
        post { requestLayout() }
    }

    override fun release() {
        isReleased = true
        unregisterSurfaceCallback()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredSize = mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredSize[0], measuredSize[1])
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registerSurfaceCallback()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unregisterSurfaceCallback()
    }

    /**
     * 注册 Surface 生命周期回调
     */
    private fun registerSurfaceCallback() {
        // 已释放或已注册时无需重复处理
        if (isReleased || isSurfaceCallbackRegistered) {
            return
        }
        holder.addCallback(mSurfaceCallback)
        isSurfaceCallbackRegistered = true
    }

    /**
     * 取消注册 Surface 生命周期回调
     */
    private fun unregisterSurfaceCallback() {
        // 尚未注册时无需处理
        if (!isSurfaceCallbackRegistered) {
            return
        }
        holder.removeCallback(mSurfaceCallback)
        isSurfaceCallbackRegistered = false
    }
}
