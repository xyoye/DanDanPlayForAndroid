package com.xyoye.stream_component.utils.screencast.receiver

import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.data.CommonJsonData
import fi.iki.elonen.NanoHTTPD

/**
 * <pre>
 *     author: xyoye1997@outlook.com
 *     time  : 2022/7/22
 *     desc  :
 * </pre>
 */

object ServerController {

    fun handleGetRequest(uri: String, parameters: Map<String, List<String>>): NanoHTTPD.Response? {
        if (uri == "/init") {
            return init()
        }
        return null
    }

    /**
     * 初始化
     */
    private fun init(): NanoHTTPD.Response {
        val jsonData = CommonJsonData(
            errorCode = 200,
            success = true
        )
        val json = JsonHelper.toJson(jsonData)
        return NanoHTTPD.newFixedLengthResponse(json)
    }
}