package com.xyoye.stream_component.utils.screencast.provider

import com.xyoye.common_component.utils.EntropyUtils
import fi.iki.elonen.NanoHTTPD

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/21
 *     desc  : 投屏内容提供方（Phone）HTTP服务器
 * </pre>
 */

class HttpServer(private val filePath: String, port: Int) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession?): Response {
        if (session != null && session.method == Method.GET) {
            return ServerController.handleGetRequest(filePath, session)
        }
        return super.serve(session)
    }

    fun getProxyUrl(): String {
        return "http://127.0.0.1:$listeningPort/${EntropyUtils.string2Md5(filePath)}"
    }
}