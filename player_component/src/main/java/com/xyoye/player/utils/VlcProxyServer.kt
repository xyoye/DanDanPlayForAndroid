package com.xyoye.player.utils

import com.xyoye.common_component.extension.formatFileName
import com.xyoye.common_component.network.helper.UnsafeOkHttpClient
import com.xyoye.common_component.utils.getFileName
import fi.iki.elonen.NanoHTTPD
import okhttp3.Request
import kotlin.random.Random

/**
 * Created by xyoye on 2021/5/2.
 */

class VlcProxyServer private constructor() : NanoHTTPD(randomPort()) {
    private lateinit var url: String
    private lateinit var headers: Map<String, String>

    private object Holder {
        val instance = VlcProxyServer()
    }

    companion object {
        //随机端口
        private fun randomPort() = Random.nextInt(30000, 40000)

        @JvmStatic
        fun getInstance() = Holder.instance
    }

    override fun serve(session: IHTTPSession?): Response {
        session?: return super.serve(session)

        val requestBuilder = Request.Builder()
        headers.forEach {
            requestBuilder.header(it.key, it.value)
        }
        session.headers.forEach {
            requestBuilder.header(it.key, it.value)
        }
        val request = requestBuilder.url(url).build()

        val call = UnsafeOkHttpClient.client.newCall(request)
        val response = call.execute()
        return newFixedLengthResponse(
            Response.Status.PARTIAL_CONTENT,
            response.header("mimeType"),
            response.body()?.byteStream(),
            response.body()?.contentLength() ?: 0
        )
    }

    fun getInputStreamUrl(url: String, headers: Map<String, String>): String {
        this.url = url
        this.headers = headers
        val fileName = getFileName(url).formatFileName().replace(" ", "_")
        return "http://127.0.0.1:$listeningPort/$fileName"
    }

}