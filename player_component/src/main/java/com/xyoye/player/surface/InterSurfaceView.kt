package com.xyoye.player.surface

import android.graphics.Bitmap
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

    fun doScreenShot(): Bitmap?

    fun release()
}