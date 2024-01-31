package com.xyoye.storage_component.utils.screencast.provider

import android.net.Uri
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.md5
import com.xyoye.common_component.extension.resourceType
import com.xyoye.common_component.network.helper.RedirectAuthorizationInterceptor
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.source.factory.StorageVideoSourceFactory
import com.xyoye.common_component.source.media.StorageVideoSource
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.RangeUtils
import com.xyoye.common_component.utils.getFileExtension
import com.xyoye.data_component.enums.ResourceType
import com.xyoye.storage_component.services.ScreencastProvideHandler
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Response
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Date

/**
 * Created by xyoye on 2022/9/15
 */

class ServerController(
    private val videoSource: StorageVideoSource,
    private val listener: ScreencastProvideHandler
) {
    private val resourceNotFound: Response by lazy {
        NanoHTTPD.newFixedLengthResponse(
            Response.Status.NOT_FOUND,
            NanoHTTPD.MIME_HTML,
            "资源不存在"
        )
    }

    private val rangeNotSatisfiable: Response by lazy {
        NanoHTTPD.newFixedLengthResponse(
            Response.Status.RANGE_NOT_SATISFIABLE,
            NanoHTTPD.MIME_HTML,
            "请求资源位置不合法"
        )
    }

    suspend fun handleSession(session: IHTTPSession): Response {
        val targetVideoSource = session.parameters["uniqueKey"]
            ?.firstOrNull()
            ?.let { findVideoSource(it) }
            ?: return resourceNotFound

        return when (session.uri) {
            "/video" -> createVideoResponse(targetVideoSource, session)
            "/danmu" -> createDanmuResponse(targetVideoSource)
            "/subtitle" -> createSubtitleResponse(targetVideoSource)
            "/callback" -> handleScreencastCallback(targetVideoSource, session)
            else -> resourceNotFound
        }
    }

    /**
     * 创建视频资源响应结果
     */
    private suspend fun createVideoResponse(
        videoSource: StorageVideoSource,
        session: IHTTPSession
    ): Response {
        val videoUrl = videoSource.getVideoUrl()

        return if (videoUrl.resourceType() == ResourceType.URL) {
            createVideoResponseRedirect(videoSource, session)
        } else {
            createVideoResponseDirect(videoSource, session)
        }.also {
            considerInvokeCallback(it)
        }
    }

    /**
     * 创建重定向的视频资源响应结果
     */
    private fun createVideoResponseRedirect(
        videoSource: BaseVideoSource,
        session: IHTTPSession
    ): Response {
        var redirectUrl = videoSource.getVideoUrl()

        // 如果链接是localHost，且请求头存在host，使用host的值替换localHost
        val localhost = "127.0.0.1"
        val isLocalhostUrl = redirectUrl.contains(localhost)
        if (isLocalhostUrl) {
            val sessionHost = session.headers["Host"] ?: session.headers["host"]
            if (!sessionHost.isNullOrEmpty()) {
                val realHost = Uri.parse("http://$sessionHost").host ?: sessionHost
                redirectUrl = redirectUrl.replace(localhost, realHost)
            }
        }

        return NanoHTTPD.newFixedLengthResponse(
            Response.Status.REDIRECT,
            getContentType(redirectUrl),
            "redirect to real source"
        ).apply {
            addHeader("Location", redirectUrl)
            addHeader(RedirectAuthorizationInterceptor.TAG_AUTH_REDIRECT, "redirect")
        }
    }

    /**
     * 创建视频资源响应结果
     */
    private suspend fun createVideoResponseDirect(
        videoSource: StorageVideoSource,
        session: IHTTPSession
    ): Response {
        val storageFile = videoSource.getStorageFile()
        val inputStream = storageFile.storage.openFile(storageFile)
            ?: return resourceNotFound

        // 解析Range
        val rangeText = session.headers["range"] ?: session.headers["Range"] ?: ""
        val range = RangeUtils.parseRange(rangeText, storageFile.fileLength())

        // 带有Range头，但解析失败，返回416
        if (rangeText.isNotEmpty() && range == null) {
            return rangeNotSatisfiable
        }

        val contentType = getContentType(storageFile.fileName())

        // Range不存在，返回200
        if (range == null) {
            return NanoHTTPD.newChunkedResponse(Response.Status.OK, contentType, inputStream)
        }

        // 尝试跳过Range的起始字节数据
        val skipped = try {
            inputStream.skip(range.first)
        } catch (e: IOException) {
            e.printStackTrace()
            -1L
        }

        // 跳过失败，返回200
        if (skipped == -1L) {
            return NanoHTTPD.newChunkedResponse(Response.Status.OK, contentType, inputStream)
        }

        // 计算Range长度
        val rangeLength = range.second - range.first + 1
        val contentRange = "bytes ${range.first}-${range.second}/$rangeLength"

        // 返回206
        return NanoHTTPD.newFixedLengthResponse(
            Response.Status.PARTIAL_CONTENT,
            contentType,
            inputStream,
            rangeLength
        ).apply {
            addHeader("Accept-Ranges", "bytes")
            addHeader("Content-Range", contentRange)
        }
    }

    /**
     * 获取内容类型
     */
    private fun getContentType(filePath: String): String {
        if (filePath.isEmpty()) {
            return "video/*"
        }
        val extension = getFileExtension(filePath)
        return "video/$extension"
    }

    /**
     * 创建弹幕资源响应结果
     */
    private fun createDanmuResponse(
        videoSource: BaseVideoSource
    ): Response {
        //弹幕文件路径为空
        val danmuPath = videoSource.getDanmu()?.danmuPath
        if (danmuPath.isNullOrEmpty()) {
            return resourceNotFound
        }
        //弹幕文件不存在
        val danmuFile = File(danmuPath)
        if (danmuFile.exists().not() || danmuFile.canRead().not()) {
            return resourceNotFound
        }

        return NanoHTTPD.newChunkedResponse(
            Response.Status.OK,
            "*/*",
            FileInputStream(danmuFile)
        ).apply {
            addHeader("episodeId", videoSource.getDanmu()?.episodeId)
            addHeader("danmuMd5", danmuFile.md5())
        }
    }

    /**
     * 创建字幕资源响应结果
     */
    private fun createSubtitleResponse(
        videoSource: BaseVideoSource
    ): Response {
        //字幕文件路径为空
        val subtitlePath = videoSource.getSubtitlePath()
        if (subtitlePath.isNullOrEmpty()) {
            return resourceNotFound
        }
        //字幕文件路径为空
        val subtitleFile = File(subtitlePath)
        if (subtitleFile.exists().not() || subtitleFile.canRead().not()) {
            return resourceNotFound
        }

        return NanoHTTPD.newChunkedResponse(
            Response.Status.OK,
            "*/*",
            FileInputStream(subtitleFile)
        ).apply {
            addHeader("subtitleSuffix", getFileExtension(subtitlePath))
            addHeader("subtitleMd5", subtitleFile.md5())
        }
    }

    /**
     * 处理视频播放回调
     */
    private suspend fun handleScreencastCallback(
        videoSource: StorageVideoSource,
        session: IHTTPSession
    ): Response {
        val position = session.parameters["position"]?.firstOrNull()?.toLongOrNull()
        val duration = session.parameters["duration"]?.firstOrNull()?.toLongOrNull()
        if (position == null || duration == null) {
            return NanoHTTPD.newFixedLengthResponse(
                Response.Status.PRECONDITION_FAILED,
                NanoHTTPD.MIME_HTML,
                "参数错误"
            )
        }

        // 修改本地播放记录中的进度
        val newHistory = videoSource.getStorageFile().playHistory
            ?.copy(videoPosition = position, videoDuration = duration, playTime = Date())
            ?: return resourceNotFound

        // 更新本地播放记录
        DatabaseManager.instance.getPlayHistoryDao().insert(newHistory)

        return NanoHTTPD.newFixedLengthResponse(
            Response.Status.OK,
            NanoHTTPD.MIME_HTML,
            "播放记录已更新"
        )
    }

    /**
     * 根据Key获取目标视频资源
     */
    private suspend fun findVideoSource(uniqueKey: String): StorageVideoSource? {
        if (videoSource.getUniqueKey() == uniqueKey) {
            return videoSource
        }

        var targetFile: StorageFile? = null
        for (index in 0 until videoSource.getGroupSize()) {
            val storageFile = videoSource.indexStorageFile(index)
            if (storageFile.uniqueKey() == uniqueKey) {
                targetFile = storageFile
                break
            }
        }
        return targetFile?.let { StorageVideoSourceFactory.create(it) }
    }

    /**
     * 考虑执行视频投屏成功回调
     */
    private fun considerInvokeCallback(response: Response) {
        if (response.status == Response.Status.OK ||
            response.status == Response.Status.PARTIAL_CONTENT ||
            response.status == Response.Status.REDIRECT
        ) {
            // 200、206、301的响应视为投屏成功
            listener.onProvideVideo(videoSource)
        }
    }
}