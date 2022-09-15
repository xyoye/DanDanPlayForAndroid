package com.xyoye.stream_component.utils.screencast.receiver

import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.data_component.enums.MediaType
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/22
 *     desc  :
 * </pre>
 */

object ServerController {

    fun handleGetRequest(
        session: NanoHTTPD.IHTTPSession,
        scope: CoroutineScope
    ): NanoHTTPD.Response? {
        val uri = session.uri
        val parameters = session.parameters
        val requestIpAddress = session.remoteIpAddress
        if (uri == "/init") {
            return init()
        } else if (uri == "/play") {
            return play(requestIpAddress, parameters, scope)
        }
        return null
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
        requestIpAddress: String,
        parameters: Map<String, List<String>>,
        scope: CoroutineScope
    ): NanoHTTPD.Response {
        var videoUrl = parameters["video_url"]?.first()
        if (videoUrl.isNullOrEmpty()) {
            return createResponse(
                code = 502,
                success = false,
                message = "资源路径为空"
            )
        }
        if (videoUrl.contains("127.0.0.1")) {
            videoUrl = videoUrl.replace("127.0.0.1", requestIpAddress)
        }

        scope.launch(Dispatchers.IO) {
            val videoSource = VideoSourceFactory.Builder()
                .setVideoSources(listOf(videoUrl))
                .create(MediaType.STREAM_LINK)
            if (videoSource == null) {
                ToastCenter.showError("播放失败，无法打开播放资源")
                return@launch
            }
            VideoSourceManager.getInstance().setSource(videoSource)
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
        }


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