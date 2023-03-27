package com.xyoye.player_component.utils

import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.annotation.RequiresApi
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.source.media.TorrentMediaSource
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.SupervisorScope
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.player.surface.InterSurfaceView
import com.xyoye.player_component.R
import kotlinx.coroutines.launch
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File
import java.util.*

/**
 * Created by xyoye on 2022/1/15.
 */

object PlayRecorder {
    private const val HEIGHT = 150f

    fun recordProgress(source: BaseVideoSource, position: Long, duration: Long) {
        SupervisorScope.IO.launch {
            var torrentPath: String? = null
            var torrentIndex = -1
            if (source is TorrentMediaSource) {
                torrentPath = source.getTorrentPath()
                torrentIndex = source.getTorrentIndex()
            }

            val history = PlayHistoryEntity(
                0,
                source.getVideoTitle(),
                source.getVideoUrl(),
                source.getMediaType(),
                position,
                duration,
                Date(),
                source.getDanmuPath(),
                source.getEpisodeId(),
                source.getSubtitlePath(),
                torrentPath,
                torrentIndex,
                JsonHelper.toJson(source.getHttpHeader()),
                null,
                source.getUniqueKey(),
                source.getStoragePath(),
                source.getStorageId()
            )

            DatabaseManager.instance.getPlayHistoryDao()
                .insert(history)

            //部分视频无法获取到视频时长，播放后再更新时长
            if (source.getMediaType() == MediaType.LOCAL_STORAGE) {
                DatabaseManager.instance
                    .getVideoDao()
                    .updateDuration(duration, source.getVideoUrl())
            }
        }
    }

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && view.surfaceTexture != null) {
                    recordTextureView(key, view)
                }
            }
            is VLCVideoLayout -> {
                val textureView = view.findViewById<TextureView>(R.id.texture_video)
                val surfaceView = view.findViewById<SurfaceView>(R.id.surface_video)
                if (textureView != null && textureView.surfaceTexture != null) {
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
        SupervisorScope.IO.launch {
            val bitmapFile = File(PathHelper.getVideoCoverDirectory(), key)
            MediaUtils.saveImage(bitmapFile, bitmap)
            bitmap.recycle()
        }
    }
}