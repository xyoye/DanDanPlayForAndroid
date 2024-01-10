package com.xyoye.common_component.network

import com.xyoye.common_component.BuildConfig
import com.xyoye.common_component.network.config.Api
import com.xyoye.common_component.network.helper.AgentInterceptor
import com.xyoye.common_component.network.helper.AuthInterceptor
import com.xyoye.common_component.network.helper.BackupDomainInterceptor
import com.xyoye.common_component.network.helper.GzipInterceptor
import com.xyoye.common_component.network.helper.LoggerInterceptor
import com.xyoye.common_component.network.helper.RemoteInterceptor
import com.xyoye.common_component.network.helper.ResDomainInterceptor
import com.xyoye.common_component.network.helper.ScreencastInterceptor
import com.xyoye.common_component.network.service.DanDanPlayService
import com.xyoye.common_component.network.service.ExtendedService
import com.xyoye.common_component.network.service.RemoteService
import com.xyoye.common_component.network.service.ResRetrofitService
import com.xyoye.common_component.network.service.ScreencastService
import com.xyoye.common_component.utils.JsonHelper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by xyoye on 2020/4/14.
 */

class Retrofit private constructor() {
    companion object {
        const val backupUrl = "http://139.217.235.62:16001/"
        private const val resUrl = "http://res.acplay.net/"
        private const val remoteUrl = "http://127.0.0.1:80/"

        val danDanPlayService = Holder.instance.danDanPlayService
        val extendedService = Holder.instance.extendedService

        val resService = Holder.instance.resRetrofitService
        val remoteService = Holder.instance.remoteRetrofitService
        val screencastService = Holder.instance.screencastService
    }

    private var danDanPlayService: DanDanPlayService
    private var extendedService: ExtendedService

    private var resRetrofitService: ResRetrofitService
    private var remoteRetrofitService: RemoteService
    private var screencastService: ScreencastService

    private val moshiConverterFactory = MoshiConverterFactory.create(JsonHelper.MO_SHI)

    init {
        danDanPlayService = Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(getOkHttpClient(needAuth = true, backup = true))
            .baseUrl(Api.DAN_DAN_PLAY)
            .build()
            .create(DanDanPlayService::class.java)

        resRetrofitService = Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(getOkHttpClient(needAuth = false, resDomain = true))
            .baseUrl(resUrl)
            .build()
            .create(ResRetrofitService::class.java)

        extendedService = Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(getOkHttpClient())
            .baseUrl(Api.PLACEHOLDER)
            .build()
            .create(ExtendedService::class.java)

        remoteRetrofitService = Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(getOkHttpClient(needAuth = false, resDomain = false, isRemote = true))
            .baseUrl(remoteUrl)
            .build()
            .create(RemoteService::class.java)

        screencastService = Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(getOkHttpClient(screencast = true))
            .baseUrl(remoteUrl)
            .build()
            .create(ScreencastService::class.java)
    }

    private object Holder {
        val instance = Retrofit()
    }

    private fun getOkHttpClient(
        needAuth: Boolean = false,
        resDomain: Boolean = false,
        isRemote: Boolean = false,
        screencast: Boolean = false,
        backup: Boolean = false,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(4, TimeUnit.SECONDS)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(AgentInterceptor())
        //token验证、gzip压缩
        if (needAuth) {
            builder.addInterceptor(AuthInterceptor())
                .addInterceptor(GzipInterceptor())
        }
        //备用服务器
        if (backup) {
            builder.addInterceptor(BackupDomainInterceptor())
        }
        //远程连接
        if (isRemote) {
            builder.addInterceptor(RemoteInterceptor())
        }
        //自定义的资源节点
        if (resDomain) {
            builder.addInterceptor(ResDomainInterceptor())
        }
        //投屏连接
        if (screencast) {
            builder.addInterceptor(ScreencastInterceptor())
        }
        //日志输出
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(LoggerInterceptor().retrofit())
        }
        return builder.build()
    }
}