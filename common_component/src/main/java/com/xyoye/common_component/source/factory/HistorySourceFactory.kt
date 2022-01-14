package com.xyoye.common_component.source.factory

import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.source.media.HistoryMediaSource
import com.xyoye.data_component.entity.PlayHistoryEntity


/**
 * Created by xyoye on 2022/1/12
 */
object HistorySourceFactory {

    fun create(builder: VideoSourceFactory.Builder): HistoryMediaSource? {
        val videoSources = builder.videoSources.filterIsInstance<PlayHistoryEntity>()
        videoSources.getOrNull(builder.index) ?: return null
        return HistoryMediaSource(videoSources, builder.index)
    }
}