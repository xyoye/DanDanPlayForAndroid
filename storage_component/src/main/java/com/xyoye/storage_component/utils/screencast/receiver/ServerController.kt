package com.xyoye.storage_component.utils.screencast.receiver

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.storage.helper.ScreencastConstants
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.data.screeencast.RemoteControlResult
import com.xyoye.common_component.utils.screencast.ScreencastRemoteControlBridge
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
    const val ACTION_DANMU_CONFIG_UPDATED = "com.xyoye.dandanplay.ACTION_DANMU_CONFIG_UPDATED"
    const val EXTRA_CONFIG_KEY = "extra_config_key"
    const val EXTRA_CONFIG_VALUE = "extra_config_value"

    fun handleGetRequest(
        context: Context,
        session: NanoHTTPD.IHTTPSession
    ): NanoHTTPD.Response? {
        return when (session.uri) {
            ScreencastConstants.ReceiverApi.init -> init(session)
            ScreencastConstants.ReceiverApi.config -> config(context, session)
            ScreencastConstants.ReceiverApi.control -> control(session, null)
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
            ScreencastConstants.ReceiverApi.play -> play(session, postData, handler)
            ScreencastConstants.ReceiverApi.control -> control(session, postData)
            else -> null
        }
    }

    /**
     * 初始化
     */
    private fun init(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val version = session.headers[ScreencastConstants.Header.versionKey]?.toIntOrNull() ?: 0
        if (version != ScreencastConstants.version) {
            return createResponse(
                NanoHTTPD.Response.Status.CONFLICT.requestStatus,
                false,
                "投屏版本不匹配，请更新双端至相同APP版本。\n" +
                    "接收端: ${ScreencastConstants.version}，投屏端: $version"
            )
        }

        return createResponse().apply {
            addHeader(ScreencastConstants.Header.versionKey, ScreencastConstants.version.toString())
        }
    }

    /**
     * 播放视频
     */
    private fun play(
        session: NanoHTTPD.IHTTPSession,
        postData: Map<String, String?>,
        handler: ScreencastReceiveHandler?
    ): NanoHTTPD.Response {
        // about postData see NanoHTTPD#parseBody(Map<String, String>)
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

    /**
     * 更新弹幕配置
     */
    private fun config(context: Context, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val parameters = session.parameters
        for (entry in parameters.entries) {
            val key = entry.key
            val value = entry.value.firstOrNull() ?: continue
            if (key.isNotEmpty() && value.isNotEmpty()) {
                sendConfigBroadcast(context, key, value)
            }
        }
        return createResponse(message = "弹幕设置已更新")
    }

    private fun control(
        session: NanoHTTPD.IHTTPSession,
        postData: Map<String, String?>?
    ): NanoHTTPD.Response {
        val mergedParams = mutableMapOf<String, String>()
        session.parameters.forEach { (key, values) ->
            val value = values.lastOrNull()
            if (value != null) {
                mergedParams[key] = value
            }
        }
        val bodyJson = postData?.get("postData")
        if (!bodyJson.isNullOrEmpty()) {
            mergedParams.putAll(JsonHelper.parseJsonMap(bodyJson))
        }

        val action = mergedParams.remove("action")
        val result = if (action.isNullOrBlank() || action.equals("status", true)) {
            ScreencastRemoteControlBridge.getStatus()
        } else {
            ScreencastRemoteControlBridge.execute(action, mergedParams)
        }
        return createResponse(result)
    }


    private fun sendConfigBroadcast(context: Context, key: String, value: Any) {
        val intent = Intent(ACTION_DANMU_CONFIG_UPDATED)
        intent.putExtra(EXTRA_CONFIG_KEY, key)
        intent.putExtra(EXTRA_CONFIG_VALUE, value.toString())
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun createResponse(
        code: Int = NanoHTTPD.Response.Status.OK.requestStatus,
        success: Boolean = true,
        message: String? = null
    ): NanoHTTPD.Response {
        val jsonData = CommonJsonData(
            errorCode = code,
            success = success,
            errorMessage = message
        )
        val json = JsonHelper.toJson(jsonData)
        return NanoHTTPD.newFixedLengthResponse(json ?: "{}")
    }

    private fun createResponse(result: RemoteControlResult): NanoHTTPD.Response {
        val json = JsonHelper.toJson(result)
        return NanoHTTPD.newFixedLengthResponse(json ?: "{}")
    }
}