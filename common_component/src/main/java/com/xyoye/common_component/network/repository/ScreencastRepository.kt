package com.xyoye.common_component.network.repository

import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.config.HeaderKey
import com.xyoye.common_component.network.request.RequestError
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.network.request.dataOrNull
import com.xyoye.common_component.services.ScreencastVersionService
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.data_component.data.screeencast.ScreencastData

/**
 * Created by xyoye on 2024/1/11.
 */

object ScreencastRepository : BaseRepository() {

    /**
     * 投屏初始化
     */
    suspend fun init(url: String, authorization: String?): Response<retrofit2.Response<CommonJsonData>> {
        val localVersion = ARouter.getInstance().navigation(ScreencastVersionService::class.java).getVersion()
        return request()
            .doGet {
                Retrofit.screencastService.init(url, authorization, localVersion)
            }.run {
                // 数据错误，外部处理
                val response = dataOrNull ?: return@run this
                val data = response.body() ?: return@run this

                // 服务器返回错误，转换为Response.Error
                if (data.success.not()) {
                    return@run Response.Error(RequestError.formJsonData(data))
                }

                // 版本版本判断
                val remoteVersion = response.headers()[HeaderKey.SCREENCAST_VERSION]?.toIntOrNull() ?: 0
                // 版本相同，返回Response.Success
                if (localVersion == remoteVersion) {
                    return@run this
                }

                // 版本不同，返回Response.Error
                val message = "投屏版本不匹配，请更新双端至相同APP版本。" +
                    "\n投屏端: ${localVersion}，接收端: $remoteVersion"
                return@run Response.Error(RequestError.formException(IllegalStateException(message)))
            }
    }

    /**
     * 投屏播放
     */
    suspend fun play(
        url: String,
        authorization: String?,
        data: ScreencastData
    ) = request()
        .json(JsonHelper.toJson(data).orEmpty())
        .doPost {
            Retrofit.screencastService.play(url, authorization, it)
        }
}