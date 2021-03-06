package com.xyoye.player.utils

import com.xyoye.common_component.utils.getFileName
import com.xyoye.data_component.bean.VideoTrackBean

/**
 * Created by xyoye on 2020/12/22.
 */

object TrackUtils {
    fun getEmptyTrack(isAudio: Boolean = false): VideoTrackBean {
        return VideoTrackBean(
            "无",
            isAudio,
            -1,
            false,
            -1,
            -1,
            null,
            true
        )
    }

    fun buildExSubtitleTrack(sourceUrl: String, isDisable: Boolean = false): VideoTrackBean {
        val trackName = getFileName(sourceUrl)
        return VideoTrackBean(
            trackName,
            false,
            -1,
            false,
            -1,
            -1,
            sourceUrl,
            true,
            isDisable
        )
    }

    fun isEmptyTrack(trackBean: VideoTrackBean): Boolean{
        return trackBean.trackId == -1 && trackBean.mSourceUrl == null
    }
}