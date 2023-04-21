package com.xyoye.common_component.network.helper

import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http.promisesBody
import okio.Buffer
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * 网络请求Log拦截器
 */
class LoggerInterceptor(tag: String = "OkHttp") : Interceptor {

    @Volatile
    private var printLevel: Level?
    private var colorLevel: java.util.logging.Level? = null
    private var logger: Logger

    init {
        printLevel = Level.NONE
        logger = Logger.getLogger(tag)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return if (printLevel == Level.NONE) {
            chain.proceed(request)
        } else {
            logForRequest(request, chain.connection())
            val startNs = System.nanoTime()
            val response: Response = try {
                chain.proceed(request)
            } catch (e: Exception) {
                log("<-- HTTP FAILED: $e")
                throw e
            }
            tookMs =
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            logForResponse(
                response,
                tookMs
            )
        }
    }

    fun setPrintLevel(level: Level?) {
        level?.apply {
            printLevel = this
        }
    }

    fun setColorLevel(level: java.util.logging.Level?) {
        level?.apply {
            colorLevel = this
        }
    }

    private fun log(message: String) {
        colorLevel?.apply {
            logger.log(this, message)
        }
    }

    private fun logForRequest(
        request: Request,
        connection: Connection?
    ) {
        val logBody = printLevel == Level.BODY
        val logHeaders = printLevel == Level.BODY || printLevel == Level.HEADERS
        val requestBody = request.body
        val protocol = connection?.protocol() ?: Protocol.HTTP_1_1
        try {
            log("--> " + request.method + ' ' + request.url + ' ' + protocol)
            if (logHeaders) {
                requestBody?.apply {
                    log("\tContent-Type: " + contentType())
                    log("\tContent-Length: " + contentLength())
                }
                val headers = request.headers
                var i = 0
                val count = headers.size
                while (i < count) {
                    val name = headers.name(i)
                    if (!"Content-Type".equals(name, ignoreCase = true) && !"Content-Length".equals(
                            name,
                            ignoreCase = true
                        )
                    ) {
                        log("\t" + name + ": " + headers.value(i))
                    }
                    ++i
                }
                log(" ")
                if (logBody) {
                    requestBody?.apply {
                        if (isPlaintext(
                                contentType()
                            )
                        ) {
                            bodyToString(request)
                        } else {
                            log("\tbody: maybe [binary body], omitted!")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            log("--> END " + request.method)
        }
    }

    private fun logForResponse(response: Response, tookMs: Long): Response {
        val builder = response.newBuilder()
        val clone = builder.build()
        var responseBody = clone.body
        val logBody =
            printLevel == Level.BODY
        val logHeaders =
            printLevel == Level.BODY || printLevel == Level.HEADERS
        try {
            log("<-- " + clone.code + ' ' + clone.message + ' ' + clone.request.url + " (" + tookMs + "ms）")
            if (logHeaders) {
                val e = clone.headers
                var bytes = 0
                val contentType = e.size
                while (bytes < contentType) {
                    log("\t" + e.name(bytes) + ": " + e.value(bytes))
                    ++bytes
                }
                log(" ")
                if (logBody && clone.promisesBody()) {
                    if (responseBody == null) {
                        return response
                    }
                    if (isPlaintext(
                            responseBody.contentType()
                        )
                    ) {
                        val byteArray =
                            toByteArray(
                                responseBody.byteStream()
                            )
                        val mediaType = responseBody.contentType()
                        val body = String(
                            byteArray,
                            getCharset(
                                mediaType
                            )
                        )
                        log("\tbody:$body")
                        responseBody = byteArray.toResponseBody(responseBody.contentType())
                        return response.newBuilder().body(responseBody).build()
                    }
                    log("\tbody: maybe [binary body], omitted!")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            log("<-- END HTTP")
        }
        return response
    }

    private fun bodyToString(request: Request) {
        try {
            val e = request.newBuilder().build()
            val body = e.body ?: return
            val buffer = Buffer()
            body.writeTo(buffer)
            val charset =
                getCharset(
                    body.contentType()
                )
            log("\tbody:" + buffer.readString(charset))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun retrofit(tag: String = "Retrofit") : LoggerInterceptor{
        printLevel = Level.BODY
        colorLevel = java.util.logging.Level.WARNING
        logger = Logger.getLogger(tag)
        return this
    }

    fun webDav(tag: String = "WebDav") : LoggerInterceptor{
        printLevel = Level.BODY
        colorLevel = java.util.logging.Level.WARNING
        logger = Logger.getLogger(tag)
        return this
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
        var tookMs: Long = 0

        private fun getCharset(contentType: MediaType?): Charset {
            return if (contentType == null) UTF8
            else contentType.charset(UTF8) ?: UTF8
        }

        private fun isPlaintext(mediaType: MediaType?): Boolean {
            return when {
                mediaType == null -> false
                mediaType.type == "text" -> true
                else -> {
                    mediaType.subtype.lowercase(Locale.getDefault()).run {
                        contains("x-www-form-urlencoded")
                                || contains("json")
                                || contains("xml")
                                || contains("html")
                    }
                }
            }
        }

        @Throws(IOException::class)
        private fun toByteArray(inputStream: InputStream): ByteArray {
            val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
            }
            outputStream.close()
            return outputStream.toByteArray()
        }
    }

    enum class Level {
        NONE, HEADERS, BODY
    }
}