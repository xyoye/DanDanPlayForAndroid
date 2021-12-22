package com.xyoye.player.surface

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.kernel.inter.AbstractVideoPlayer
import com.xyoye.player.utils.RenderMeasureHelper

/**
 * Created by xyoye on 2020/11/3.
 */

class RenderTextureView(context: Context) : TextureView(context), InterSurfaceView {

    private val mMeasureHelper = RenderMeasureHelper()
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mSurface: Surface? = null

    private lateinit var mVideoPlayer: AbstractVideoPlayer

    private val listener = object : SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = false

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            mSurfaceTexture = surface
            mSurface = Surface(surface)
            if (this@RenderTextureView::mVideoPlayer.isInitialized) {
                mVideoPlayer.setSurface(mSurface!!)
            }
        }
    }

    init {
        surfaceTextureListener = listener
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

    override fun doScreenShot() = bitmap

    override fun release() {
        mSurface?.release()
        mSurfaceTexture?.release()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredSize = mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredSize[0], measuredSize[1])
    }
}