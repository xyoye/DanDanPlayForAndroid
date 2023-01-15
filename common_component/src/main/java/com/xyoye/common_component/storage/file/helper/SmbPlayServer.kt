package com.xyoye.common_component.storage.file.helper

import com.xyoye.common_component.storage.file.impl.SmbStorageFile
import com.xyoye.common_component.storage.impl.SmbStorage
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.RangeUtils
import com.xyoye.common_component.utils.getFileExtension
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder
import kotlin.random.Random

/**
 * Created by xyoye on 2023/1/15.
 */

class SmbPlayServer private constructor() : NanoHTTPD(randomPort()) {

    private var mStorageFile: SmbStorageFile? = null
    private var mStorage: SmbStorage? = null
    private var mContentType: String = "video/*"
    private var mInputStream: InputStream? = null

    private val resourceNotFound by lazy {
        resourceNotFoundResponse()
    }
    private val resourceOpenFailed by lazy {
        resourceOpenFailedResponse()
    }

    private object Holder {
        val instance = SmbPlayServer()
    }

    companion object {

        //随机端口
        private fun randomPort() = Random.nextInt(20000, 30000)

        @JvmStatic
        fun getInstance() = Holder.instance
    }

    override fun serve(session: IHTTPSession): Response {
        val storage = mStorage ?: return resourceNotFound
        val storageFile = mStorageFile ?: return resourceNotFound

        //关闭之前打开的数据流
        IOUtils.closeIO(mInputStream)

        //重新打开数据流，重要，否则无法设置offset
        val inputStream = getInputStream(storage, storageFile)
            ?: return resourceOpenFailed
        mInputStream = inputStream

        //解析Range
        val rangeText = session.headers["range"] ?: session.headers["Range"]
        val rangeArray = rangeText?.run {
            RangeUtils.getRange(this, storageFile.fileLength)
        }

        //存在range，且contentLength != 0
        return if (rangeArray != null && rangeArray[2] != 0L) {
            getPartialResponse(inputStream, rangeArray, storageFile.fileLength)
        } else {
            getOKResponse(inputStream)
        }
    }

    private fun getInputStream(storage: SmbStorage, file: SmbStorageFile) = runBlocking {
        storage.openFile(file)
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
            mContentType,
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
            mContentType,
            inputStream
        )
    }

    private fun resourceNotFoundResponse(): Response {
        return newFixedLengthResponse(
            Response.Status.NOT_FOUND,
            "*/*",
            "resource not found"
        )
    }

    private fun resourceOpenFailedResponse(): Response {
        return newFixedLengthResponse(
            Response.Status.INTERNAL_ERROR,
            "*/*",
            "open resource failed"
        )
    }

    private fun getContentType(filePath: String): String {
        if (filePath.isEmpty()) {
            return "video/*"
        }
        val extension = getFileExtension(filePath)
        return "video/$extension"
    }

    suspend fun startSync(timeoutMs: Long = 5000): Boolean {
        if (wasStarted()) {
            return true
        }
        return withTimeout(timeoutMs) {
            start()
            while (isActive) {
                if (wasStarted()) {
                    return@withTimeout true
                }
            }
            stop()
            return@withTimeout false
        }
    }

    fun generatePlayUrl(
        storage: SmbStorage,
        storageFile: SmbStorageFile
    ): String {
        mStorage = storage
        mStorageFile = storageFile
        mContentType = getContentType(storageFile.filePath())
        val encodeFileName = URLEncoder.encode(storageFile.fileName(), "utf-8")
        return "http://127.0.0.1:$listeningPort/$encodeFileName"
    }

    fun release() {
        IOUtils.closeIO(mInputStream)
        this@SmbPlayServer.stop()
    }
}