package com.xyoye.player.utils

import com.xyoye.common_component.network.helper.UnsafeOkHttpClient
import com.xyoye.common_component.utils.getFileName
import fi.iki.elonen.NanoHTTPD
import okhttp3.Request
import java.net.URLEncoder
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
        session ?: return super.serve(session)

        val proxyResponse = getProxyResponse(session)
        val response = newFixedLengthResponse(
            Response.Status.lookup(proxyResponse.code),
            proxyResponse.header("Content-Type"),
            proxyResponse.body?.byteStream(),
            proxyResponse.body?.contentLength() ?: 0
        )
        val headers = proxyResponse.headers

        for (index in 0 until headers.size) {
            val key = headers.name(index)
            val value = headers.value(index)
            response.addHeader(key, value)
        }

        return response
    }

    fun getInputStreamUrl(url: String, headers: Map<String, String>): String {
        this.url = url
        this.headers = headers
        val encodeFileName = URLEncoder.encode(getFileName(url), "utf-8")
        return "http://127.0.0.1:$listeningPort/$encodeFileName"
    }

    private fun getProxyResponse(session: IHTTPSession): okhttp3.Response {
        val requestBuilder = Request.Builder()
        headers.forEach {
            requestBuilder.header(it.key, it.value)
        }
        session.headers.forEach {
            requestBuilder.header(it.key, it.value)
        }
        //移除VLC到本地服务器的部分请求头，这部分会影响后续请求
        requestBuilder.apply {
            removeHeader("host")
            removeHeader("remote-addr")
            removeHeader("http-client-ip")
        }
        val request = requestBuilder.url(url).build()

        val call = UnsafeOkHttpClient.client.newCall(request)
        return call.execute()
    }

}