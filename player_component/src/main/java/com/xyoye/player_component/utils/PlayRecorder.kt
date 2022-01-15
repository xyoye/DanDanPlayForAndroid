package com.xyoye.player_component.utils

import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.annotation.RequiresApi
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.player.surface.InterSurfaceView
import com.xyoye.player_component.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File

/**
 * Created by xyoye on 2022/1/15.
 */

object PlayRecorder {
    private const val HEIGHT = 150f

    fun recordImage(key: String, renderView: InterSurfaceView?) {
        val view = renderView?.getView()
            ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return
        }
        try {
            recordTextureView(key, view)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun recordTextureView(key: String, view: View) {
        when (view) {
            is SurfaceView -> {
                recordSurfaceView(key, view)
            }
            is TextureView -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    recordTextureView(key, view)
                }
            }
            is VLCVideoLayout -> {
                val textureView = view.findViewById<TextureView>(R.id.texture_video)
                val surfaceView = view.findViewById<SurfaceView>(R.id.surface_video)
                if (textureView != null) {
                    recordTextureView(key, textureView)
                } else if (surfaceView != null) {
                    recordSurfaceView(key, surfaceView)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun recordSurfaceView(key: String, surfaceView: SurfaceView) {
        val recordBitmap = createBitmap(surfaceView)
        PixelCopy.request(surfaceView.holder.surface, recordBitmap, { result ->
            if (result == PixelCopy.SUCCESS) {
                saveBitmap(key, recordBitmap)
            }
        }, Handler(Looper.getMainLooper()))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun recordTextureView(key: String, textureView: TextureView) {
        val recordBitmap = createBitmap(textureView)
        val surface = Surface(textureView.surfaceTexture)
        PixelCopy.request(surface, recordBitmap, { result ->
            if (result == PixelCopy.SUCCESS) {
                saveBitmap(key, recordBitmap)
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun createBitmap(view: View): Bitmap {
        val height: Float
        val width: Float
        if (view.height > view.width) {
            width = HEIGHT
            height = width / view.width * view.height
        } else {
            height = HEIGHT
            width = height / view.height * view.width
        }

        return Bitmap.createBitmap(
            width.toInt(),
            height.toInt(),
            Bitmap.Config.RGB_565
        )
    }

    private fun saveBitmap(key: String, bitmap: Bitmap) {
        GlobalScope.launch(Dispatchers.IO) {
            val bitmapFile = File(PathHelper.getVideoCoverDirectory(), key)
            MediaUtils.saveImage(bitmapFile, bitmap)
            bitmap.recycle()
        }
    }
}