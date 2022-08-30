package com.xyoye.stream_component.utils.screencast.provider

import fi.iki.elonen.NanoHTTPD
import kotlin.random.Random

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/21
 *     desc  : 投屏内容提供方（Phone）HTTP服务器
 * </pre>
 */

class HttpServer : NanoHTTPD(randomPort()){

    companion object {
        //随机端口
        private fun randomPort() = Random.nextInt(20000, 30000)
    }

    override fun serve(session: IHTTPSession?): Response {
        return super.serve(session)
    }
}