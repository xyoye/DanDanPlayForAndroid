package com.xyoye.player.surface

import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.xyoye.data_component.enums.SurfaceType
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.kernel.impl.vlc.VlcVideoPlayer
import com.xyoye.player.kernel.inter.AbstractVideoPlayer
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

/**
 * Created by xyoye on 2021/4/12.
 */

class RenderVLCView(
    context: Context
) : InterSurfaceView {

    private lateinit var mVideoPlayer: VlcVideoPlayer

    private val vlcLayout = VLCVideoLayout(context)

    // 是否已释放渲染视图
    private var isReleased = false

    // 宿主界面是否已经进入暂停状态
    private var isHostPaused = false

    // 是否需要在 Surface 可用后重新绑定 VLC 渲染载体
    private var isAttachRequested = false

    // 视频 SurfaceView 是否已收到有效 Surface 的创建回调
    private var isVideoSurfaceCreated = false

    // VLCVideoLayout 内部的视频 SurfaceView
    private var videoSurfaceView: SurfaceView? = null

    // 在 Surface 创建回调完成后执行 VLC 渲染载体绑定
    private val attachRenderViewRunnable = Runnable {
        // 已释放、播放器未初始化或 Surface 已失效时，不执行排队中的绑定任务
        if (isReleased || isHostPaused || !this::mVideoPlayer.isInitialized) {
            return@Runnable
        }
        // 视频 SurfaceView 尚未创建时，继续等待对应的创建回调
        if (PlayerInitializer.surfaceType == SurfaceType.VIEW_SURFACE && !areSurfacesCreated()) {
            return@Runnable
        }
        isAttachRequested = false
        mVideoPlayer.attachRenderView(vlcLayout)
    }

    // 监听 VLCVideoLayout 内部 Surface 的真实创建和销毁时机
    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            // 只有视频 Surface 决定播放器何时可以恢复，字幕 Surface 由 VLC 自行管理
            isVideoSurfaceCreated = holder.surface.isValid

            // 宿主恢复或 View 换窗后，等待新 Surface 创建完成再重新绑定
            if (isAttachRequested && areSurfacesCreated()) {
                requestRenderViewAttach()
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            // SurfaceView UPDATE 完成后才允许 VLC 获取新的 native Surface
            isVideoSurfaceCreated = holder.surface.isValid

            // 部分设备复用 SurfaceView 时只回调 surfaceChanged，不会重新回调 surfaceCreated
            if (isAttachRequested && areSurfacesCreated()) {
                requestRenderViewAttach()
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // 销毁后继续保留绑定请求，等待下一次 surfaceCreated 再执行
            isVideoSurfaceCreated = false
            vlcLayout.removeCallbacks(attachRenderViewRunnable)
        }
    }

    override fun attachPlayer(player: AbstractVideoPlayer) {
        isReleased = false
        mVideoPlayer = (player as VlcVideoPlayer)
        player.attachRenderView(vlcLayout)
        observeVideoSurface()
    }

    override fun setVideoSize(videoWidth: Int, videoHeight: Int) {

    }

    override fun setVideoRotation(degree: Int) {

    }

    override fun setScaleType(screenScale: VideoScreenScale) {
        val scale = when (screenScale) {
            VideoScreenScale.SCREEN_SCALE_16_9 -> MediaPlayer.ScaleType.SURFACE_16_9
            VideoScreenScale.SCREEN_SCALE_4_3 -> MediaPlayer.ScaleType.SURFACE_4_3
            VideoScreenScale.SCREEN_SCALE_CENTER_CROP -> MediaPlayer.ScaleType.SURFACE_FIT_SCREEN
            VideoScreenScale.SCREEN_SCALE_ORIGINAL -> MediaPlayer.ScaleType.SURFACE_ORIGINAL
            VideoScreenScale.SCREEN_SCALE_MATCH_PARENT -> MediaPlayer.ScaleType.SURFACE_FILL
            VideoScreenScale.SCREEN_SCALE_DEFAULT -> MediaPlayer.ScaleType.SURFACE_BEST_FIT
        }
        mVideoPlayer.setScale(scale)
    }

    override fun getView(): View {
        return vlcLayout
    }

    override fun refresh() {
        // Activity 回前台时由 onHostResume 统一恢复，避免 exitPopupMode 提前绑定旧 Surface
        if (isReleased || isHostPaused || !this::mVideoPlayer.isInitialized) {
            return
        }
        mVideoPlayer.prepareRenderViewReattach()
        requestRenderViewAttach()
    }

    override fun onHostResume() {
        // 首次进入界面时已经完成初始化绑定，无需重复操作
        if (!isHostPaused) {
            return
        }
        isHostPaused = false
        // 后台播放可能已经在无 Surface 状态恢复，回前台只需绑定新 Surface
        mVideoPlayer.prepareRenderViewReattach()
        requestRenderViewAttach()
    }

    override fun onHostPause(keepPlaying: Boolean) {
        isHostPaused = true
        isAttachRequested = false
        vlcLayout.removeCallbacks(attachRenderViewRunnable)

        // onResume 早于 SurfaceView 更新，必须等待回前台后的真实 Surface 回调
        if (PlayerInitializer.surfaceType == SurfaceType.VIEW_SURFACE) {
            isVideoSurfaceCreated = false
        }

        // 播放器尚未初始化时，不存在需要解绑的 VLC 渲染载体
        if (!this::mVideoPlayer.isInitialized) {
            return
        }
        mVideoPlayer.prepareRenderViewReattach(keepPlaying)
    }

    override fun release() {
        isReleased = true
        isAttachRequested = false
        vlcLayout.removeCallbacks(attachRenderViewRunnable)
        videoSurfaceView?.holder?.removeCallback(surfaceCallback)
        videoSurfaceView = null
        isVideoSurfaceCreated = false
    }

    /**
     * 监听 VLCVideoLayout 创建的 SurfaceView 生命周期。
     */
    private fun observeVideoSurface() {
        // TextureView 由 libVLC 自身监听 SurfaceTexture 可用状态
        if (PlayerInitializer.surfaceType != SurfaceType.VIEW_SURFACE) {
            return
        }
        val videoSurface = vlcLayout.findViewById<SurfaceView>(org.videolan.R.id.surface_video)
        videoSurfaceView?.holder?.removeCallback(surfaceCallback)
        videoSurfaceView = videoSurface
        isVideoSurfaceCreated = videoSurface.holder.surface.isValid
        videoSurface.holder.addCallback(surfaceCallback)
    }

    /**
     * 在当前渲染载体可用后重新绑定 VLC 视频输出。
     */
    private fun requestRenderViewAttach() {
        // 暂停期间不允许 refresh 或排队任务抢先绑定仍将被销毁的 Surface
        if (isReleased || isHostPaused || !this::mVideoPlayer.isInitialized) {
            return
        }
        isAttachRequested = true
        vlcLayout.removeCallbacks(attachRenderViewRunnable)

        // SurfaceView 必须等待新的 surfaceCreated 回调，避免绑定已废弃的 BufferQueue
        if (PlayerInitializer.surfaceType == SurfaceType.VIEW_SURFACE && !areSurfacesCreated()) {
            return
        }
        vlcLayout.post(attachRenderViewRunnable)
    }

    /**
     * 判断 VLC 使用的视频 SurfaceView 是否已经创建有效 Surface。
     *
     * @return 视频 Surface 可用时返回 true
     */
    private fun areSurfacesCreated(): Boolean {
        return isVideoSurfaceCreated && videoSurfaceView?.holder?.surface?.isValid == true
    }
}
