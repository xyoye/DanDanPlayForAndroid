package com.xyoye.common_component.network.helper

import android.annotation.SuppressLint
import com.xyoye.common_component.BuildConfig
import okhttp3.OkHttpClient
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Created by xyoye on 2021/5/2.
 *
 * 忽略证书验证的OkHttpClient
 */

object UnsafeOkHttpClient {

    private val unSafeTrustManager = object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }

    private val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(unSafeTrustManager), null)
    }

    val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, unSafeTrustManager)
            .hostnameVerifier { _, _ -> true }
            .addNetworkInterceptor(RedirectAuthorizationInterceptor())
        if (BuildConfig.DEBUG) {
            builder.addNetworkInterceptor(LoggerInterceptor().webDav())
        }
        return@lazy builder.build()
    }
}