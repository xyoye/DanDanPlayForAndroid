package com.xyoye.player.surface

import android.content.Context

/**
 * Created by xyoye on 2020/11/3.
 */

class TextureViewFactory : SurfaceFactory(){

    override fun createRenderView(context: Context): InterSurfaceView {
        return RenderTextureView(context)
    }

}