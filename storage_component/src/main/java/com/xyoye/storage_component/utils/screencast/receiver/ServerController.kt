package com.xyoye.storage_component.utils.screencast.receiver

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.storage.helper.ScreencastConstants
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
            updateDanmuConfig(context, key, value)
        }
        return createResponse(message = "弹幕设置已更新")
    }

    private fun updateDanmuConfig(context: Context, key: String, value: String) {
        when (key) {
            "danmuSize" -> {
                val intValue = value.toIntOrNull() ?: return
                DanmuConfig.putDanmuSize(intValue)
                sendConfigBroadcast(context, key, intValue)
            }
            "danmuSpeed" -> {
                val intValue = value.toIntOrNull() ?: return
                DanmuConfig.putDanmuSpeed(intValue)
                sendConfigBroadcast(context, key, intValue)
            }
            "danmuAlpha" -> {
                val intValue = value.toIntOrNull() ?: return
                DanmuConfig.putDanmuAlpha(intValue)
                sendConfigBroadcast(context, key, intValue)
            }
            "danmuStoke" -> {
                val intValue = value.toIntOrNull() ?: return
                DanmuConfig.putDanmuStoke(intValue)
                sendConfigBroadcast(context, key, intValue)
            }
            "showMobileDanmu" -> {
                val boolValue = value.toBoolean()
                DanmuConfig.putShowMobileDanmu(boolValue)
                sendConfigBroadcast(context, key, boolValue)
            }
            "showBottomDanmu" -> {
                val boolValue = value.toBoolean()
                DanmuConfig.putShowBottomDanmu(boolValue)
                sendConfigBroadcast(context, key, boolValue)
            }
            "showTopDanmu" -> {
                val boolValue = value.toBoolean()
                DanmuConfig.putShowTopDanmu(boolValue)
                sendConfigBroadcast(context, key, boolValue)
            }
            "danmuMaxCount" -> {
                val intValue = value.toIntOrNull() ?: return
                DanmuConfig.putDanmuMaxCount(intValue)
                sendConfigBroadcast(context, key, intValue)
            }
            "danmuMaxLine" -> {
                val intValue = value.toIntOrNull() ?: return
                DanmuConfig.putDanmuMaxLine(intValue)
                sendConfigBroadcast(context, key, intValue)
            }
            "danmuScrollMaxLine" -> {
                val intValue = value.toIntOrNull() ?: return
                DanmuConfig.putDanmuScrollMaxLine(intValue)
                sendConfigBroadcast(context, key, intValue)
            }
            "danmuTopMaxLine" -> {
                val intValue = value.toIntOrNull() ?: return
                DanmuConfig.putDanmuTopMaxLine(intValue)
                sendConfigBroadcast(context, key, intValue)
            }
            "danmuBottomMaxLine" -> {
                val intValue = value.toIntOrNull() ?: return
                DanmuConfig.putDanmuBottomMaxLine(intValue)
                sendConfigBroadcast(context, key, intValue)
            }
            "cloudDanmuBlock" -> {
                val boolValue = value.toBoolean()
                DanmuConfig.putCloudDanmuBlock(boolValue)
                sendConfigBroadcast(context, key, boolValue)
            }
            "danmuLanguage" -> {
                val intValue = value.toIntOrNull() ?: return
                DanmuConfig.putDanmuLanguage(intValue)
                sendConfigBroadcast(context, key, intValue)
            }
        }
    }

    private fun sendConfigBroadcast(context: Context, key: String, value: Any) {
        val intent = Intent(ACTION_DANMU_CONFIG_UPDATED)
        intent.putExtra(EXTRA_CONFIG_KEY, key)
        when (value) {
            is Int -> intent.putExtra(EXTRA_CONFIG_VALUE, value)
            is Boolean -> intent.putExtra(EXTRA_CONFIG_VALUE, value)
            is String -> intent.putExtra(EXTRA_CONFIG_VALUE, value)
            is Float -> intent.putExtra(EXTRA_CONFIG_VALUE, value)
            is Long -> intent.putExtra(EXTRA_CONFIG_VALUE, value)
        }
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
        return NanoHTTPD.newFixedLengthResponse(json)
    }
}