package com.xyoye.stream_component.utils.screencast.provider

import android.net.Uri
import com.xyoye.common_component.extension.md5
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.utils.RangeUtils
import com.xyoye.common_component.utils.getFileExtension
import com.xyoye.stream_component.services.ScreencastProvideHandler
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Created by xyoye on 2022/9/15
 */

object ServerController {
    private val resourceNotFound: NanoHTTPD.Response by lazy {
        NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.NOT_FOUND,
            NanoHTTPD.MIME_HTML,
            "资源不存在"
        )
    }

    fun handleGetRequest(
        videoSource: BaseVideoSource,
        session: NanoHTTPD.IHTTPSession,
        handler: ScreencastProvideHandler?
    ): NanoHTTPD.Response {
        val index = session.parameters["index"]
            ?.firstOrNull()
            ?.toIntOrNull()
            ?: return resourceNotFound

        val targetVideoSource = if (videoSource.getGroupIndex() == index)
            videoSource
        else
            getIndexSource(index, videoSource) ?: return resourceNotFound

        return when (session.uri) {
            "/video" -> createVideoResponse(targetVideoSource, session, handler)
            "/danmu" -> createDanmuResponse(targetVideoSource)
            "/subtitle" -> createSubtitleResponse(targetVideoSource)
            else -> resourceNotFound
        }
    }

    /**
     * 创建视频资源响应结果
     */
    private fun createVideoResponse(
        videoSource: BaseVideoSource,
        session: NanoHTTPD.IHTTPSession,
        handler: ScreencastProvideHandler?
    ): NanoHTTPD.Response {
        val videoUrl = videoSource.getVideoUrl()
        return if (videoUrl.startsWith("http://", true) || videoUrl.startsWith("https://")) {
            createVideoResponseRedirect(videoSource, session, handler)
        } else {
            createVideoResponseDirect(videoSource, session, handler)
        }
    }

    /**
     * 创建视频资源响应结果-通用
     */
    private fun createVideoResponseDirect(
        videoSource: BaseVideoSource,
        session: NanoHTTPD.IHTTPSession,
        handler: ScreencastProvideHandler?
    ): NanoHTTPD.Response {
        //视频文件不存在
        val videoFile = File(videoSource.getVideoUrl())
        if (videoFile.exists().not() || videoFile.canRead().not()) {
            return resourceNotFound
        }

        //已投送视频
        handler?.onProvideVideo(videoSource)

        //解析Range
        val rangeText = session.headers["range"] ?: session.headers["Range"]
        val rangeArray = rangeText?.run {
            RangeUtils.getRange(this, videoFile.length())
        }

        val videoInputStream = FileInputStream(videoFile)
        val contentType = getContentType(videoFile.absolutePath)
        //存在range，且contentLength != 0
        return if (rangeArray != null && rangeArray[2] != 0L) {
            createVideoResponsePartial(
                videoInputStream,
                rangeArray,
                contentType,
                videoFile.length()
            )
        } else {
            createVideoResponseTotal(videoInputStream, contentType)
        }
    }

    /**
     * 创建视频资源响应结果-重定向
     */
    private fun createVideoResponseRedirect(
        videoSource: BaseVideoSource,
        session: NanoHTTPD.IHTTPSession,
        handler: ScreencastProvideHandler?
    ): NanoHTTPD.Response {
        var redirectUrl = videoSource.getVideoUrl()
        val localhost = "127.0.0.1"
        val isLocalhostUrl = redirectUrl.contains(localhost)
        if (isLocalhostUrl) {
            var host = session.headers["Host"] ?: session.headers["host"]
            if (host != null && host.isNotEmpty()) {
                host = Uri.parse("http://$host").host ?: host
                redirectUrl = redirectUrl.replace(localhost, host)
            }
        }

        //已投送视频
        handler?.onProvideVideo(videoSource)

        return NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.REDIRECT,
            getContentType(redirectUrl),
            "redirect to real source"
        ).apply {
            addHeader("Location", redirectUrl)
        }
    }

    /**
     * 创建视频资源响应结果-分块
     */
    private fun createVideoResponsePartial(
        inputStream: InputStream,
        rangeArray: Array<Long>,
        contentType: String,
        sourceLength: Long
    ): NanoHTTPD.Response {
        try {
            //设置Offset
            inputStream.skip(rangeArray[0])
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //响应内容
        val response = NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.PARTIAL_CONTENT,
            contentType,
            inputStream,
            sourceLength
        )
        //添加响应头
        val contentRange = "bytes ${rangeArray[0]}-${rangeArray[1]}/$sourceLength"
        response.addHeader("Accept-Ranges", "bytes")
        response.addHeader("Content-Range", contentRange)
        response.addHeader("Content-Length", rangeArray[2].toString())
        return response
    }

    /**
     * 创建视频资源响应结果-完整
     */
    private fun createVideoResponseTotal(
        inputStream: InputStream,
        contentType: String
    ): NanoHTTPD.Response {
        return NanoHTTPD.newChunkedResponse(
            NanoHTTPD.Response.Status.OK,
            contentType,
            inputStream
        )
    }

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
    ): NanoHTTPD.Response {
        //弹幕文件路径为空
        val danmuPath = videoSource.getDanmuPath()
        if (danmuPath.isNullOrEmpty()) {
            return resourceNotFound
        }
        //弹幕文件不存在
        val danmuFile = File(danmuPath)
        if (danmuFile.exists().not() || danmuFile.canRead().not()) {
            return resourceNotFound
        }

        return NanoHTTPD.newChunkedResponse(
            NanoHTTPD.Response.Status.OK,
            "*/*",
            FileInputStream(danmuFile)
        ).apply {
            addHeader("episodeId", videoSource.getEpisodeId().toString())
            addHeader("danmuMd5", danmuFile.md5())
        }
    }

    /**
     * 创建字幕资源响应结果
     */
    private fun createSubtitleResponse(
        videoSource: BaseVideoSource
    ): NanoHTTPD.Response {
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
            NanoHTTPD.Response.Status.OK,
            "*/*",
            FileInputStream(subtitleFile)
        ).apply {
            addHeader("subtitleSuffix", getFileExtension(subtitlePath))
            addHeader("subtitleMd5", subtitleFile.md5())
        }
    }

    /**
     * 根据索引获取目标视频资源
     */
    private fun getIndexSource(index: Int, videoSource: BaseVideoSource) = runBlocking {
        return@runBlocking withContext(Dispatchers.IO) {
            videoSource.indexSource(index)
        }
    }
}