package com.xyoye.player.surface

import android.content.Context
import com.xyoye.data_component.enums.SurfaceType

/**
 * Created by xyoye on 2020/11/3.
 */

abstract class SurfaceFactory {

    companion object{
        fun getFactory(surfaceType: SurfaceType): SurfaceFactory{
            return when(surfaceType){
                SurfaceType.VIEW_SURFACE -> SurfaceViewFactory()
                SurfaceType.VIEW_TEXTURE -> TextureViewFactory()
                else -> SurfaceViewFactory()
            }
        }
    }

    abstract fun createRenderView(context: Context): InterSurfaceView
}