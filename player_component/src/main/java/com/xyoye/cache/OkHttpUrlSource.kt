package com.xyoye.cache

import android.text.TextUtils
import com.danikula.videocache.*
import com.danikula.videocache.headers.HeaderInjector
import com.danikula.videocache.source.Source
import com.danikula.videocache.sourcestorage.SourceInfoStorage
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2021/12/23
 *     desc  :
 * </pre>
 */
class OkHttpUrlSource : Source {
    private var sourceInfo: SourceInfo
    private val headerInjector: HeaderInjector
    private var sourceInfoStorage: SourceInfoStorage
    private var client = OkHttpClient()

    private var inputStream: InputStream? = null

    companion object {
        private const val MAX_REDIRECTS = 5
        private const val DEFAULT_CONTENT_LENGTH = Int.MIN_VALUE.toLong()
    }

    constructor(
        url: String,
        config: Config
    ) {
        this.sourceInfoStorage = config.sourceInfoStorage
        this.headerInjector = config.headerInjector
        this.sourceInfo = sourceInfoStorage.get(url) ?: SourceInfo(
            url,
            DEFAULT_CONTENT_LENGTH,
            ProxyCacheUtils.getSupposablyMime(url)
        )
    }

    private constructor(source: OkHttpUrlSource) {
        sourceInfo = source.sourceInfo
        headerInjector = source.headerInjector
        sourceInfoStorage = source.sourceInfoStorage
        client = source.client
    }

    override fun open(offset: Long) {
        try {
            val response = openConnection(offset, -1)
            inputStream = BufferedInputStream(response.body()?.byteStream())

            val length = readSourceAvailableBytes(response, offset)
            val mineType = response.header("Content-Type")
            sourceInfo = SourceInfo(sourceInfo.url, length, mineType)
            sourceInfoStorage.put(sourceInfo.url, sourceInfo)
        } catch (e: IOException) {
            throw ProxyCacheException(
                "Error opening connection for " + sourceInfo.url + " with offset " + offset, e
            )
        }
    }

    override fun length(): Long {
        if (sourceInfo.length == DEFAULT_CONTENT_LENGTH) {
            fetchContentInfo()
        }
        return sourceInfo.length
    }

    override fun read(buffer: ByteArray?): Int {
        if (inputStream == null) {
            throw ProxyCacheException("Error reading data from ${sourceInfo.url}: connection is absent!")
        }
        return try {
            inputStream!!.read(buffer, 0, buffer!!.size)
        } catch (e: InterruptedIOException) {
            throw InterruptedProxyCacheException(
                "Reading source ${sourceInfo.url} is interrupted",
                e
            )
        } catch (e: IOException) {
            throw ProxyCacheException("Error reading data from ${sourceInfo.url}", e)
        }
    }

    override fun close() {
        ProxyCacheUtils.close(inputStream)
    }

    @Throws(ProxyCacheException::class)
    private fun fetchContentInfo() {
        Log.d("Read content info from " + sourceInfo.url)
        try {
            val response = openConnection(0, 10000, true)
            if (response.isSuccessful.not()) {
                ProxyCacheUtils.close(response.body()?.byteStream())
                throw ProxyCacheException("Fail to fetchContentInfo: ${sourceInfo.url}")
            }

            val length = response.body()?.contentLength() ?: DEFAULT_CONTENT_LENGTH
            val mineType = response.body()?.contentType().toString()
            ProxyCacheUtils.close(response.body()?.byteStream())
            sourceInfo = SourceInfo(sourceInfo.url, length, mineType)
            sourceInfoStorage.put(sourceInfo.url, sourceInfo)
            Log.d("Source info fetched: $sourceInfo")
        } catch (e: IOException) {
            Log.e("Error fetching info from ${sourceInfo.url}", e)
        }
    }

    @Throws(IOException::class, ProxyCacheException::class)
    private fun openConnection(offset: Long, timeout: Long, headerOnly: Boolean = false): Response {
        if (timeout > 0 && client.readTimeoutMillis().toLong() != timeout) {
            client = client.newBuilder()
                .callTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build()
        }

        var redirectCount = 0
        var url = sourceInfo.url

        while (redirectCount < MAX_REDIRECTS) {
            Log.d("Open connection " + (if (offset > 0) " with offset $offset" else "") + " to " + url)
            val requestBuilder = Request.Builder()
                .url(url)
            injectCustomHeaders(requestBuilder, url)
            if (offset > 0) {
                requestBuilder.addHeader("Range", "bytes=$offset-")
            }
            if (headerOnly) {
                requestBuilder.method("HEAD", null)
            } else {
                requestBuilder.get()
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (response.isRedirect.not()){
                return response
            }

            url = response.header("Location")
            redirectCount++
        }
        throw ProxyCacheException("Too many redirects: $redirectCount")
    }

    private fun injectCustomHeaders(requestBuilder: Request.Builder, url: String) {
        val extraHeaders = headerInjector.addHeaders(url)
        for ((key, value) in extraHeaders) {
            requestBuilder.addHeader(key, value)
        }
    }

    @Synchronized
    @Throws(ProxyCacheException::class)
    override fun getMime(): String? {
        if (TextUtils.isEmpty(sourceInfo.mime)) {
            fetchContentInfo()
        }
        return sourceInfo.mime
    }

    @Throws(IOException::class)
    private fun readSourceAvailableBytes(
        response: Response,
        offset: Long
    ): Long {
        val contentLength: Long = getContentLength(response)
        return when (response.code()) {
            HttpURLConnection.HTTP_OK -> contentLength
            HttpURLConnection.HTTP_PARTIAL -> contentLength + offset
            else -> sourceInfo.length
        }
    }

    private fun getContentLength(response: Response): Long {
        val contentLengthValue = response.header("Content-Length")
        return contentLengthValue?.toLong() ?: -1
    }

    override fun getUrl(): String = sourceInfo.url

    override fun cloneNew(): Source {
        return OkHttpUrlSource(this)
    }

    override fun toString(): String {
        return "OkHttpUrlSource{sourceInfo='$sourceInfo}"
    }
}