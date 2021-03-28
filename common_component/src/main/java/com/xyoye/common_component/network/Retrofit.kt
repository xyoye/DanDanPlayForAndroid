package com.xyoye.common_component.network

import com.xyoye.common_component.BuildConfig
import com.xyoye.common_component.network.helper.*
import com.xyoye.common_component.network.service.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by xyoye on 2020/4/14.
 */

class Retrofit private constructor() {
    companion object {
        private const val baseUrl = "https://api.acplay.net/"
        private const val resUrl = "http://res.acplay.net/"
        private const val shooterUrl = "http://api.assrt.net/"
        private const val torrentUrl = "https://m2t.chinacloudsites.cn/"
        private const val remoteUrl = "http://127.0.0.1:80/"

        val service = Holder.instance.retrofitService
        val resService = Holder.instance.resRetrofitService
        val extService = Holder.instance.extRetrofitService
        val torrentService = Holder.instance.torrentRetrofitService
        val remoteService = Holder.instance.remoteRetrofitService
    }

    private var retrofitService: RetrofitService
    private var resRetrofitService: ResRetrofitService
    private var extRetrofitService: ExtRetrofitService
    private var torrentRetrofitService: TorrentRetrofitService
    private var remoteRetrofitService: RemoteService

    init {
        retrofitService = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .client(getOkHttpClient(needAuth = true))
            .baseUrl(baseUrl)
            .build()
            .create(RetrofitService::class.java)

        resRetrofitService = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .client(getOkHttpClient(needAuth = false, resDomain = true))
            .baseUrl(resUrl)
            .build()
            .create(ResRetrofitService::class.java)

        extRetrofitService = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .client(getOkHttpClient())
            .baseUrl(shooterUrl)
            .build()
            .create(ExtRetrofitService::class.java)

        torrentRetrofitService = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .client(getOkHttpClient())
            .baseUrl(torrentUrl)
            .build()
            .create(TorrentRetrofitService::class.java)

        remoteRetrofitService = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .client(getOkHttpClient(needAuth = false, resDomain = false, isRemote = true))
            .baseUrl(remoteUrl)
            .build()
            .create(RemoteService::class.java)
    }

    private object Holder {
        val instance = Retrofit()
    }

    private fun getOkHttpClient(
        needAuth: Boolean = false,
        resDomain: Boolean = false,
        isRemote: Boolean = false
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
            builder.addInterceptor(RemoteInterceptor.getInstance())
        }
        //自定义的资源节点
        if (resDomain) {
            builder.addInterceptor(ResDomainInterceptor())
        }
        //日志输出
        if (BuildConfig.IS_DEBUG_MODE) {
            builder.addInterceptor(LoggerInterceptor().retrofit())
        }
        return builder.build()
    }
}