package com.xyoye.common_component.source.media

import android.net.Uri
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.source.inter.ExtraSource
import com.xyoye.common_component.source.inter.VideoSource
import com.xyoye.common_component.utils.UriUtils
import com.xyoye.common_component.utils.getFileName
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2021/11/21.
 */

class OuterMediaSource private constructor(
    videoUri: Uri,
    private val videoUrl: String,
    private val currentPosition: Long,
    private var danmuPath: String?,
    private var episodeId: Int,
    private var subtitlePath: String?,
) : VideoSource, ExtraSource {
    private val videoTitle = UriUtils.queryVideoTitle(BaseApplication.getAppContext(), videoUri)
        ?: getFileName(videoUrl)

    companion object {
        fun build(
            videoUri: Uri,
            videoUrl: String,
            historyEntity: PlayHistoryEntity?,
            danmuPath: String?,
            episodeId: Int,
        ): OuterMediaSource {
            return OuterMediaSource(
                videoUri,
                videoUrl,
                historyEntity?.videoPosition ?: 0L,
                danmuPath,
                episodeId,
                historyEntity?.subtitlePath
            )
        }
    }

    override fun getDanmuPath(): String? {
        return danmuPath
    }

    override fun setDanmuPath(path: String) {
        danmuPath = path
    }

    override fun getEpisodeId(): Int {
        return episodeId
    }

    override fun setEpisodeId(id: Int) {
        episodeId = id
    }

    override fun getSubtitlePath(): String? {
        return subtitlePath
    }

    override fun setSubtitlePath(path: String) {
        subtitlePath = path
    }

    override fun getVideoUrl(): String {
        return videoUrl
    }

    override fun getVideoTitle(): String {
        return videoTitle
    }

    override fun getCurrentPosition(): Long {
        return currentPosition
    }

    override fun getMediaType(): MediaType {
        return MediaType.OTHER_STORAGE
    }

    override fun getHttpHeader(): Map<String, String>? {
        return null
    }

}