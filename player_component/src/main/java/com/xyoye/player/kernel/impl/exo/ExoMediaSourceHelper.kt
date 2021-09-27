package com.xyoye.player.kernel.impl.exo

import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.utils.PathHelper
import java.util.*

/**
 * Created by xyoye on 2020/10/30.
 */

object ExoMediaSourceHelper {

    private lateinit var mCache: Cache

    private val mUserAgent: String = Util.getUserAgent(
        BaseApplication.getAppContext(),
        BaseApplication.getAppContext().applicationInfo.name
    )
    private val mHttpDataSourceFactory = DefaultHttpDataSourceFactory(
        mUserAgent,
        null,
        DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
        DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
        //http->https重定向支持
        true
    )

    fun getMediaSource(uri: String): MediaSource {
        return getMediaSource(uri, null, false)
    }

    fun getMediaSource(uri: String, headers: Map<String, String>?): MediaSource {
        return getMediaSource(uri, headers, false)
    }

    fun getMediaSource(uri: String, isCache: Boolean): MediaSource {
        return getMediaSource(uri, null, isCache)
    }

    fun getMediaSource(uri: String, headers: Map<String, String>?, isCache: Boolean): MediaSource {
        val contentUri = Uri.parse(uri)
        val mediaItem = MediaItem.fromUri(contentUri)

        // TODO: 2021/9/26 RTMP-2.15.1扩展不在MavenCentral
//        if ("rtmp" == contentUri.scheme) {
//            return ProgressiveMediaSource.Factory(RtmpDataSourceFactory())
//                .createMediaSource(mediaItem)
//        }

        headers?.let { setHeaders(it) }

        val dataSourceFactory = if (isCache)
            getCacheDataSourceFactory()
        else
            DefaultDataSourceFactory(BaseApplication.getAppContext(), mHttpDataSourceFactory)

        return when (inferContentType(uri)) {
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            C.TYPE_SS -> SsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            else -> ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }
    }

    private fun getCacheDataSourceFactory(): DataSource.Factory {
        if (!this::mCache.isInitialized) {
            mCache = newCache()
        }

        return CacheDataSource.Factory()
            .setCache(mCache)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            .setUpstreamDataSourceFactory(
                DefaultDataSourceFactory(
                    BaseApplication.getAppContext(),
                    mHttpDataSourceFactory
                )
            )
    }

    private fun newCache(): Cache {
        return SimpleCache(
            PathHelper.getExoCacheDirectory(),
            LeastRecentlyUsedCacheEvictor(512L * 1024 * 1024),
            ExoDatabaseProvider(BaseApplication.getAppContext())
        )
    }

    private fun inferContentType(fileName: String): Int {
        val name = fileName.lowercase(Locale.getDefault())
        return when {
            name.contains(".mpd") -> {
                C.TYPE_DASH
            }
            name.contains(".m3u8") -> {
                C.TYPE_HLS
            }
            name.matches(".*\\.ism(l)?(/manifest(\\(.+\\))?)?".toRegex()) -> {
                C.TYPE_SS
            }
            else -> {
                C.TYPE_OTHER
            }
        }
    }

    private fun setHeaders(headers: Map<String, String>) {
        for (entry in headers.entries) {
            if (entry.key == "User-Agent") {
                mHttpDataSourceFactory.javaClass.getDeclaredField("userAgent").apply {
                    isAccessible = true
                    set(mHttpDataSourceFactory, entry.value)
                }
            } else {
                mHttpDataSourceFactory.defaultRequestProperties.set(entry.key, entry.value)
            }
        }
    }

    fun setCache(cache: Cache) {
        mCache = cache
    }
}