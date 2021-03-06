package com.xyoye.player.surface

import android.content.Context

/**
 * Created by xyoye on 2020/11/3.
 */

class SurfaceViewFactory : SurfaceFactory(){

    override fun createRenderView(context: Context): InterSurfaceView {
        return RenderSurfaceView(context)
    }

}