package com.xyoye.stream_component.utils.screencast.provider

import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.stream_component.services.ScreencastProvideHandler
import fi.iki.elonen.NanoHTTPD

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/21
 *     desc  : 投屏内容提供方（Phone）HTTP服务器
 * </pre>
 */

class HttpServer(private val videoSource: BaseVideoSource, port: Int) : NanoHTTPD(port) {
    private var handler: ScreencastProvideHandler? = null

    override fun serve(session: IHTTPSession?): Response {
        if (session != null && session.method == Method.GET) {
            return ServerController.handleGetRequest(videoSource, session, handler)
        }
        return super.serve(session)
    }

    fun setScreenProvideHandler(handler: ScreencastProvideHandler) {
        this.handler = handler
    }
}