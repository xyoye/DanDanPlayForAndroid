package com.xyoye.stream_component.utils.screencast.provider

import com.xyoye.common_component.utils.EntropyUtils
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.RangeUtils
import com.xyoye.common_component.utils.getFileExtension
import com.xyoye.data_component.data.CommonJsonData
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/9/15
 *     desc  :
 * </pre>
 */

object ServerController {

    fun handleGetRequest(
        videoPath: String,
        session: NanoHTTPD.IHTTPSession
    ): NanoHTTPD.Response {
        val videoId = EntropyUtils.string2Md5(videoPath)
        if (session.uri != "/$videoId") {
            return createResponse(code = 404, success = false, "资源不存在")
        }

        val videoFile = File(videoPath)
        if (videoFile.exists().not() || videoFile.canRead().not()) {
            return createResponse(code = 404, success = false, "资源不存在")
        }

        //解析Range
        val rangeText = session.headers["range"] ?: session.headers["Range"]
        val rangeArray = rangeText?.run {
            RangeUtils.getRange(this, videoFile.length())
        }

        val videoInputStream = FileInputStream(videoFile)
        //存在range，且contentLength != 0
        return if (rangeArray != null && rangeArray[2] != 0L) {
            createPartialResponse(
                videoInputStream,
                rangeArray,
                getContentType(videoPath),
                videoFile.length()
            )
        } else {
            createOKResponse(videoInputStream, getContentType(videoPath))
        }
    }


    private fun createPartialResponse(
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

    private fun createOKResponse(
        inputStream: InputStream,
        contentType: String
    ): NanoHTTPD.Response {
        return NanoHTTPD.newChunkedResponse(
            NanoHTTPD.Response.Status.OK,
            contentType,
            inputStream
        )
    }

    private fun createResponse(
        code: Int = 200,
        success: Boolean = true,
        message: String? = null
    ): NanoHTTPD.Response {
        val jsonData = CommonJsonData(
            errorCode = code,
            success = success,
            errorMessage = message
        )
        val json = JsonHelper.toJson(jsonData)
        return NanoHTTPD.newFixedLengthResponse(json)
    }

    private fun getContentType(filePath: String): String {
        if (filePath.isEmpty()) {
            return "video/*"
        }
        val extension = getFileExtension(filePath)
        return "video/$extension"
    }
}