package com.xyoye.common_component.network

import com.xyoye.common_component.BuildConfig
import com.xyoye.common_component.network.helper.*
import com.xyoye.common_component.network.service.*
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
        private const val baseUrl = "https://api.dandanplay.net/"
        private const val resUrl = "http://res.acplay.net/"
        private const val shooterUrl = "http://api.assrt.net/"
        private const val torrentUrl = "https://m2t.dandanplay.net/"
        private const val remoteUrl = "http://127.0.0.1:80/"

        val service = Holder.instance.retrofitService
        val resService = Holder.instance.resRetrofitService
        val extService = Holder.instance.extRetrofitService
        val torrentService = Holder.instance.torrentRetrofitService
        val remoteService = Holder.instance.remoteRetrofitService
        val screencastService = Holder.instance.screencastService
    }

    private var retrofitService: RetrofitService
    private var resRetrofitService: ResRetrofitService
    private var extRetrofitService: ExtRetrofitService
    private var torrentRetrofitService: TorrentRetrofitService
    private var remoteRetrofitService: RemoteService
    private var screencastService: ScreencastService

    private val moshiConverterFactory = MoshiConverterFactory.create(JsonHelper.MO_SHI)

    init {
        retrofitService = Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(getOkHttpClient(needAuth = true))
            .baseUrl(baseUrl)
            .build()
            .create(RetrofitService::class.java)

        resRetrofitService = Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(getOkHttpClient(needAuth = false, resDomain = true))
            .baseUrl(resUrl)
            .build()
            .create(ResRetrofitService::class.java)

        extRetrofitService = Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(getOkHttpClient())
            .baseUrl(shooterUrl)
            .build()
            .create(ExtRetrofitService::class.java)

        torrentRetrofitService = Retrofit.Builder()
            .addConverterFactory(moshiConverterFactory)
            .client(getOkHttpClient())
            .baseUrl(torrentUrl)
            .build()
            .create(TorrentRetrofitService::class.java)

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
        if (BuildConfig.IS_DEBUG_MODE) {
            builder.addInterceptor(LoggerInterceptor().retrofit())
        }
        return builder.build()
    }
}