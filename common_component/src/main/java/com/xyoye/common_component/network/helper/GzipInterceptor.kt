package com.xyoye.common_component.network.helper

import com.xyoye.common_component.utils.GZIPUtils
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Created by xyoye on 2020/8/20.
 */

class GzipInterceptor : Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val responseBody = response.body
        if (response.code == 200 && responseBody != null){
            var data : ByteArray? = null
            if (GZIPUtils.isGzip(response.headers)){
                data = GZIPUtils.uncompress(responseBody.bytes())
            }
            if (data != null){
                return response.newBuilder()
                    .body(data.toResponseBody(responseBody.contentType()))
                    .build()
            }
        }
        return response
    }
}