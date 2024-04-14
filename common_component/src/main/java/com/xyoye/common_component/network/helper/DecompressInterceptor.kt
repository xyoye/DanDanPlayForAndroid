package com.xyoye.common_component.network.helper

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.BufferedSource
import okio.GzipSource
import okio.InflaterSource
import okio.buffer
import java.util.zip.Inflater

/**
 * Created by xyoye on 2024/4/15
 */

class DecompressInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())

        val originalBody = originalResponse.body
            ?: return originalResponse
        val originalEncoding = originalResponse.header("Content-Encoding")
            ?: return originalResponse

        val decompressEncoding = DecompressEncoding.formEncoding(originalEncoding)
            ?: return originalResponse

        return originalResponse.newBuilder()
            .body(DecompressResponseBody(originalBody, decompressEncoding))
            .removeHeader("Content-Encoding")
            .build()
    }

    private enum class DecompressEncoding(val encoding: String) {
        GZIP("gzip"),

        DEFLATE("deflate");

        companion object {
            fun formEncoding(encoding: String): DecompressEncoding? {
                return values().firstOrNull {
                    it.encoding.equals(encoding, ignoreCase = true)
                }
            }
        }
    }

    private class DecompressResponseBody(
        private val responseBody: ResponseBody,
        private val decompressEncoding: DecompressEncoding
    ) : ResponseBody() {
        override fun contentLength(): Long {
            return responseBody.contentLength()
        }

        override fun contentType(): MediaType? {
            return responseBody.contentType()
        }

        override fun source(): BufferedSource {
            return when (decompressEncoding) {
                DecompressEncoding.DEFLATE -> InflaterSource(responseBody.source(), Inflater(true))
                DecompressEncoding.GZIP -> GzipSource(responseBody.source())
            }.buffer()
        }
    }
}