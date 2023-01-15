package com.xyoye.common_component.source.base

import androidx.annotation.CheckResult
import com.xyoye.common_component.source.factory.*
import com.xyoye.data_component.enums.MediaType


/**
 * Created by xyoye on 2022/1/11
 */
object VideoSourceFactory {

    class Builder {
        var videoSources: List<Any> = mutableListOf()
            private set
        var extraSources: List<Any> = mutableListOf()
            private set
        var rootPath: String = ""
            private set
        var index: Int = 0
            private set
        var httpHeaders: Map<String, String> = emptyMap()
            private set
        var mediaType: MediaType = MediaType.OTHER_STORAGE
            private set

        @CheckResult
        fun setVideoSources(sources: List<Any>): Builder {
            this.videoSources = sources
            return this
        }

        @CheckResult
        fun setExtraSource(sources: List<Any>): Builder {
            this.extraSources = sources
            return this
        }

        @CheckResult
        fun setRootPath(path: String): Builder {
            this.rootPath = path
            return this
        }

        @CheckResult
        fun setIndex(index: Int): Builder {
            this.index = index
            return this
        }


        @CheckResult
        fun setHttpHeaders(headers: Map<String, String>): Builder {
            this.httpHeaders = headers
            return this
        }

        suspend fun create(mediaType: MediaType): BaseVideoSource? {
            this.mediaType = mediaType
            return when (mediaType) {
                MediaType.FTP_SERVER -> FTPSourceFactory.create(this)
                MediaType.MAGNET_LINK -> TorrentSourceFactory.create(this)
                MediaType.REMOTE_STORAGE -> RemoteSourceFactory.create(this)
                MediaType.LOCAL_STORAGE -> LocalSourceFactory.create(this)
                MediaType.SCREEN_CAST -> ScreencastSourceFactory.create(this)
                MediaType.STREAM_LINK,
                MediaType.OTHER_STORAGE -> StreamSourceFactory.create(this)
                else -> null
            }
        }

        fun createHistory(): BaseVideoSource? {
            return HistorySourceFactory.create(this)
        }
    }
}