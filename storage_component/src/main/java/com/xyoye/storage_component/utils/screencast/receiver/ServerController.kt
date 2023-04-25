package com.xyoye.storage_component.utils.screencast.receiver

import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.data_component.data.screeencast.ScreencastData
import com.xyoye.storage_component.services.ScreencastReceiveHandler
import fi.iki.elonen.NanoHTTPD
import java.io.IOException

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/22
 *     desc  :
 * </pre>
 */

object ServerController {

    fun handleGetRequest(
        session: NanoHTTPD.IHTTPSession
    ): NanoHTTPD.Response? {
        return when (session.uri) {
            "/init" -> init()
            else -> null
        }
    }

    fun handlePostRequest(
        session: NanoHTTPD.IHTTPSession,
        handler: ScreencastReceiveHandler?
    ): NanoHTTPD.Response? {
        val postData: Map<String, String?> = HashMap()
        try {
            session.parseBody(postData)
        } catch (ioe: IOException) {
            return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
                NanoHTTPD.MIME_PLAINTEXT,
                "SERVER INTERNAL ERROR: IOException: " + ioe.message
            )
        } catch (re: NanoHTTPD.ResponseException) {
            return NanoHTTPD.newFixedLengthResponse(re.status, NanoHTTPD.MIME_PLAINTEXT, re.message)
        }


        return when (session.uri) {
            "/play" -> play(session, postData, handler)
            else -> null
        }
    }

    /**
     * 初始化
     */
    private fun init(): NanoHTTPD.Response {
        return createResponse()
    }

    /**
     * 播放视频
     */
    private fun play(
        session: NanoHTTPD.IHTTPSession,
        postData: Map<String, String?>,
        handler: ScreencastReceiveHandler?
    ): NanoHTTPD.Response {
        val screencastData = postData["postData"]?.run {
            JsonHelper.parseJson<ScreencastData>(this)
        } ?: return createResponse(
            code = 502,
            success = false,
            message = "无法读取资源"
        )
        screencastData.apply { ip = session.remoteIpAddress }
        handler?.onReceiveVideo(screencastData)
        return createResponse()
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
}