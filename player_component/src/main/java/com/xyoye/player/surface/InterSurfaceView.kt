package com.xyoye.player.surface

import android.view.View
import com.xyoye.data_component.enums.VideoScreenScale
import com.xyoye.player.kernel.inter.AbstractVideoPlayer

/**
 * Created by xyoye on 2020/11/3.
 */

interface InterSurfaceView {

    fun attachPlayer(player: AbstractVideoPlayer)

    fun setVideoSize(videoWidth: Int, videoHeight: Int)

    fun setVideoRotation(degree: Int)

    fun setScaleType(screenScale: VideoScreenScale)

    fun getView(): View

    fun refresh()

    /**
     * 宿主界面恢复可见时刷新渲染载体。
     */
    fun onHostResume() = Unit

    /**
     * 宿主界面不可见时释放渲染载体绑定。
     *
     * @param keepPlaying 是否需要在后台继续播放
     */
    fun onHostPause(keepPlaying: Boolean = false) = Unit

    fun release()
}
