package com.xyoye.common_component.storage.file.helper

import com.xyoye.common_component.storage.file.impl.FtpStorageFile
import com.xyoye.common_component.storage.impl.FtpStorage
import com.xyoye.common_component.utils.RangeUtils
import com.xyoye.common_component.utils.getFileExtension
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.net.URLEncoder
import kotlin.random.Random

/**
 * Created by XYJ on 2023/1/17.
 */

class FtpPlayServer private constructor() : NanoHTTPD(randomPort()) {
    //FTP暂时无法处理range
    private val skipEnable = false

    private var mStorageFile: FtpStorageFile? = null
    private var mStorage: FtpStorage? = null
    private var mContentType: String = "video/*"

    private val resourceNotFound by lazy {
        resourceNotFoundResponse()
    }
    private val resourceOpenFailed by lazy {
        resourceOpenFailedResponse()
    }

    private object Holder {
        val instance = FtpPlayServer()
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
        storage.completePending()

        //解析Range
        val rangeText = session.headers["range"] ?: session.headers["Range"]
        val rangeArray = rangeText?.run {
            RangeUtils.getRange(this, storageFile.fileLength())
        }

        //存在range，且contentLength != 0
        return if (skipEnable && rangeArray != null && rangeArray[2] != 0L) {
            getPartialResponse(storage, storageFile, rangeArray, storageFile.fileLength())
        } else {
            getOKResponse(storage, storageFile)
        }
    }

    private fun getInputStream(
        storage: FtpStorage,
        file: FtpStorageFile,
        offset: Long = -1
    ) = runBlocking {
        storage.openFile(file, offset)
    }

    private fun getPartialResponse(
        storage: FtpStorage,
        storageFile: FtpStorageFile,
        rangeArray: Array<Long>,
        sourceLength: Long
    ): Response {
        val inputStream = getInputStream(storage, storageFile, rangeArray[0])
            ?: return resourceOpenFailed
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

    private fun getOKResponse(
        storage: FtpStorage,
        storageFile: FtpStorageFile
    ): Response {
        val inputStream = getInputStream(storage, storageFile)
            ?: return resourceOpenFailed
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
        storage: FtpStorage,
        storageFile: FtpStorageFile
    ): String {
        mStorage = storage
        mStorageFile = storageFile
        mContentType = getContentType(storageFile.filePath())
        val encodeFileName = URLEncoder.encode(storageFile.fileName(), "utf-8")
        return "http://127.0.0.1:$listeningPort/$encodeFileName"
    }
}