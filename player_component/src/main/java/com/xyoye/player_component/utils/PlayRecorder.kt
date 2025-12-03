package com.xyoye.player_component.utils

import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.xyoye.common_component.config.UserConfig
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.resumeWhenAlive
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.common_component.network.repository.ResourceRepository
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.source.media.StorageVideoSource
import com.xyoye.common_component.storage.file.impl.ScreencastStorageFile
import com.xyoye.common_component.storage.helper.ScreencastConstants
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.SupervisorScope
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.player.surface.InterSurfaceView
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File
import java.util.Date

/**
 * Created by xyoye on 2022/1/15.
 */

object PlayRecorder {
    private const val HEIGHT = 150f

    fun recordProgress(source: BaseVideoSource, position: Long, duration: Long) {
        SupervisorScope.IO.launch {
            var torrentPath: String? = null
            var torrentIndex = -1

            if (source is StorageVideoSource) {
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
                source.getDanmu()?.danmuPath,
                source.getDanmu()?.episodeId,
                source.getSubtitlePath(),
                torrentPath,
                torrentIndex,
                JsonHelper.toJson(source.getHttpHeader()),
                source.getUniqueKey(),
                source.getStoragePath(),
                source.getStorageId(),
                source.getAudioPath()
            )

            // 保存播放历史到数据库
            DatabaseManager.instance.getPlayHistoryDao()
                .insert(history)
            // 上报剧集播放进度到投屏端
            recordToScreencastProvider(source, history)
            // 上报剧集播放到云端
            recordToCloud(source)

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
        SupervisorScope.IO.launch {
            val bitmap = try {
                generateRenderImage(view)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } ?: return@launch

            val bitmapFile = File(PathHelper.getVideoCoverDirectory(), key)
            MediaUtils.saveImage(bitmapFile, bitmap)
            bitmap.recycle()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun generateRenderImage(view: View, imageSize: Point? = null): Bitmap? {
        when (view) {
            is SurfaceView -> {
                return recordSurfaceView(view, imageSize)
            }

            is TextureView -> {
                if (view.surfaceTexture != null) {
                    return recordTextureView(view, imageSize)
                }
            }

            is VLCVideoLayout -> {
                val textureView = view.findViewById<TextureView>(org.videolan.R.id.texture_video)
                val surfaceView = view.findViewById<SurfaceView>(org.videolan.R.id.surface_video)
                if (textureView != null && textureView.surfaceTexture != null) {
                    return recordTextureView(textureView, imageSize)
                } else if (surfaceView != null) {
                    return recordSurfaceView(surfaceView, imageSize)
                }
            }
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun recordSurfaceView(
        surfaceView: SurfaceView,
        imageSize: Point?
    ) = suspendCancellableCoroutine {
        val recordBitmap = createBitmap(surfaceView, imageSize)
        if (recordBitmap == null) {
            it.resumeWhenAlive(null)
            return@suspendCancellableCoroutine
        }

        val surface = surfaceView.holder.surface
        if (surface.isValid.not()) {
            it.resumeWhenAlive(null)
            return@suspendCancellableCoroutine
        }

        PixelCopy.request(surface, recordBitmap, { result ->
            if (result == PixelCopy.SUCCESS) {
                it.resumeWhenAlive(recordBitmap)
            } else {
                it.resumeWhenAlive(null)
            }
        }, Handler(Looper.getMainLooper()))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun recordTextureView(
        textureView: TextureView,
        imageSize: Point?
    ) = suspendCancellableCoroutine {
        val recordBitmap = createBitmap(textureView, imageSize)
        if (recordBitmap == null) {
            it.resumeWhenAlive(null)
            return@suspendCancellableCoroutine
        }

        val surfaceTexture = textureView.surfaceTexture
        if (surfaceTexture == null) {
            it.resumeWhenAlive(null)
            return@suspendCancellableCoroutine
        }

        val surface = Surface(textureView.surfaceTexture)
        if (surface.isValid.not()) {
            it.resumeWhenAlive(null)
            return@suspendCancellableCoroutine
        }

        PixelCopy.request(surface, recordBitmap, { result ->
            if (result == PixelCopy.SUCCESS) {
                it.resumeWhenAlive(recordBitmap)
            } else {
                it.resumeWhenAlive(null)
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun createBitmap(view: View, imageSize: Point?): Bitmap? {
        if (imageSize != null && imageSize.x > 0 && imageSize.y > 0) {
            return Bitmap.createBitmap(
                imageSize.x,
                imageSize.y,
                Bitmap.Config.ARGB_8888
            )
        }

        if (view.width <= 0 || view.height <= 0) {
            return null
        }

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

    /**
     * 上报剧集播放记录到云端
     */
    private suspend fun recordToCloud(videoSource: BaseVideoSource) {
        val episodeId = videoSource.getDanmu()?.episodeId
        if (episodeId.isNullOrEmpty()) {
            return
        }

        if (UserConfig.getUserLoggedIn().not()) {
            return
        }

        AnimeRepository.addEpisodePlayHistory(listOf(episodeId))
    }

    /**
     * 上报剧集播放进度到投屏端
     */
    private suspend fun recordToScreencastProvider(
        source: BaseVideoSource,
        history: PlayHistoryEntity
    ) {
        // 仅处理媒体库视频源
        if (source !is StorageVideoSource) {
            return
        }

        // 仅处理投屏文件
        val storageFile = source.getStorageFile()
        if (storageFile !is ScreencastStorageFile) {
            return
        }

        // 构建包含播放进度的回调地址
        val callbackUrl = storageFile.getCallbackUrl()
        val completeUrl = callbackUrl.toUri().buildUpon()
            .appendQueryParameter(ScreencastConstants.Param.position, history.videoPosition.toString())
            .appendQueryParameter(ScreencastConstants.Param.duration, history.videoDuration.toString())
            .build()
            .toString()

        // 发送请求，忽略结果
        ResourceRepository.getResourceResponseBody(completeUrl)
    }
}