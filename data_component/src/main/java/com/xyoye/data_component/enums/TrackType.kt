package com.xyoye.data_component.enums

import com.xyoye.data_component.bean.LocalDanmuBean

/**
 * Created by xyoye on 2024/1/23.
 */

enum class TrackType {
    DANMU,

    SUBTITLE,

    AUDIO;

    fun getSubtitle(value: Any?): String? {
        if (value != null && value is String && this == SUBTITLE) {
            return value
        }
        return null
    }

    fun getAudio(value: Any?): String? {
        if (value != null && value is String && this == AUDIO) {
            return value
        }
        return null
    }

    fun getDanmu(value: Any?): LocalDanmuBean? {
        if (value != null && value is LocalDanmuBean && this == DANMU) {
            return value
        }
        return null
    }
}