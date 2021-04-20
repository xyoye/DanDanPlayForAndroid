package com.xyoye.stream_component.utils.server

import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.getFileExtension
import com.xyoye.common_component.utils.getFileName
import com.xyoye.stream_component.utils.RangeUtils
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random

class SMBPlayServer private constructor() : NanoHTTPD(randomPort()) {

    private var sourcePath: String? = null
    private var sourceContentType: String? = null
    private var sourceLength: Long = 0
    private var inputStreamBlock: ((filePath: String) -> InputStream)? = null

    private var videoInputStream: InputStream? = null

    private object Holder {
        val instance = SMBPlayServer()
    }

    companion object {
        //随机端口
        private fun randomPort() = Random.nextInt(20000, 30000)

        @JvmStatic
        fun getInstance() = Holder.instance
    }

    override fun serve(session: IHTTPSession?): Response {
        //请求可解析，资源存在
        if (session != null && inputStreamBlock != null && sourcePath?.isNotEmpty() == true) {

            //关闭之前打开的数据流
            closeIO()

            //重新打开数据流，重要，否则无法设置offset
            val videoInputStream = inputStreamBlock!!.invoke(sourcePath!!)

            //解析Range
            val rangeText = session.headers["range"] ?: session.headers["Range"]
            val rangeArray = rangeText?.run {
                RangeUtils.getRange(this, sourceLength)
            }

            //存在range，且contentLength != 0
            return if (rangeArray != null && rangeArray[2] != 0L) {
                getPartialResponse(videoInputStream, rangeArray, sourceLength)
            } else {
                getOKResponse(videoInputStream)
            }
        }
        return super.serve(session)
    }

    fun getInputStreamUrl(
        fileName: String,
        filePath: String,
        fileLength: Long,
        streamBlock: (filePath: String) -> InputStream
    ): String {
        sourcePath = filePath
        sourceContentType = getContentType(filePath)
        sourceLength = fileLength
        inputStreamBlock = streamBlock
        return "http://127.0.0.1:$listeningPort/$fileName"
    }

    fun closeIO() {
        if (videoInputStream != null) {
            IOUtils.closeIO(videoInputStream)
            videoInputStream = null
        }
    }

    private fun getPartialResponse(
        inputStream: InputStream,
        rangeArray: Array<Long>,
        sourceLength: Long
    ): Response {
        try {
            //设置Offset
            inputStream.skip(rangeArray[0])
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //响应内容
        val response = newFixedLengthResponse(
            Response.Status.PARTIAL_CONTENT,
            sourceContentType,
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

    private fun getOKResponse(inputStream: InputStream): Response {
        return newChunkedResponse(
            Response.Status.OK,
            sourceContentType,
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
}