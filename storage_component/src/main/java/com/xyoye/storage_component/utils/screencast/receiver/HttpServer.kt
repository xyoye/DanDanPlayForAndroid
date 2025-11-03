package com.xyoye.storage_component.utils.screencast.receiver

import com.xyoye.common_component.extension.aesDecode
import com.xyoye.common_component.network.config.HeaderKey
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.storage_component.services.ScreencastReceiveHandler
import fi.iki.elonen.NanoHTTPD

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/21
 *     desc  : 投屏内容接收方（TV）HTTP服务器
 * </pre>
 */

import android.content.Context

class HttpServer(
    private val context: Context,
    private val password: String?,
    port: Int
) : NanoHTTPD(port) {
    private var handler: ScreencastReceiveHandler? = null

    override fun serve(session: IHTTPSession?): Response {
        if (session != null) {
            // 处理 OPTIONS 请求（CORS 预检）
            if (session.method == Method.OPTIONS) {
                return handleOptions(session)
            }

            //身份验证
            if (authentication(session).not()) {
                return unauthorizedResponse()
            }

            val response = when (session.method) {
                Method.GET -> {
                    ServerController.handleGetRequest(context, session)
                }

                Method.POST -> {
                    ServerController.handlePostRequest(session, handler)
                }

                else -> {
                    null
                }
            }
            if (response != null) {
                return addCorsHeaders(response)
            }
        }
        return super.serve(session)
    }

    private fun handleOptions(session: IHTTPSession): Response {
        val response = newFixedLengthResponse("").apply {
            mimeType = "application/json"
        }
        return addCorsHeaders(response)
    }

    private fun addCorsHeaders(response: Response): Response {
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, screencast-version")
        response.addHeader("Access-Control-Max-Age", "3600")
        return response
    }

    private fun authentication(session: IHTTPSession): Boolean {
        if (password.isNullOrEmpty()) {
            return true
        }

        val authorization = session.headers[HeaderKey.AUTHORIZATION.lowercase()]
        if (authorization.isNullOrEmpty()) {
            return false
        }
        if (authorization.startsWith("Bearer ").not()) {
            return false
        }
        try {
            return password == authorization.substring("Bearer ".length).aesDecode()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun unauthorizedResponse(): Response {
        val jsonData = CommonJsonData(
            errorCode = 401,
            success = false,
            errorMessage = "连接验证失败"
        )
        val json = JsonHelper.toJson(jsonData)
        return newFixedLengthResponse(json).apply {
            mimeType = "application/json"
        }
    }

    fun setScreenReceiveHandler(handler: ScreencastReceiveHandler?) {
        this.handler = handler
    }
}