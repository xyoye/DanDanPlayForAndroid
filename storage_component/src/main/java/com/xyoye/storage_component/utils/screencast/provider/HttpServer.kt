package com.xyoye.storage_component.utils.screencast.provider

import com.xyoye.common_component.source.media.StorageVideoSource
import com.xyoye.storage_component.services.ScreencastProvideHandler
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/21
 *     desc  : 投屏内容提供方（Phone）HTTP服务器
 * </pre>
 */

class HttpServer(
    port: Int,
    private val videoSource: StorageVideoSource,
    private val coroutineScope: CoroutineScope,
    private val listener: ScreencastProvideHandler
) : NanoHTTPD(port) {

    private val controller: ServerController by lazy {
        ServerController(videoSource, listener)
    }

    override fun serve(session: IHTTPSession?): Response {
        if (session != null && session.method == Method.GET) {
            return runBlocking(coroutineScope.coroutineContext) {
                controller.handleSession(session)
            }
        }
        return super.serve(session)
    }
}