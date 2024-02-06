package com.xyoye.data_component.bean

import android.net.Uri
import com.xyoye.data_component.enums.TrackType

/**
 * Created by xyoye on 2020/11/16.
 */

data class VideoTrackBean(
    // 轨道ID
    val id: String? = null,

    // 轨道名称
    val name: String,

    // 轨道类型
    val type: TrackType,

    // 外部轨道资源
    val trackResource: Any? = null,

    // 是否被选中
    val selected: Boolean = false,

    // 是否是禁用轨道
    val disable: Boolean = false,

    // 是否是内部轨道
    val internal: Boolean = false
) {

    companion object {
        fun internal(id: String, name: String, type: TrackType, selected: Boolean): VideoTrackBean {
            return VideoTrackBean(id = id, name = name, type = type, selected = selected, internal = true)
        }

        fun subtitle(subtitlePath: String): VideoTrackBean {
            val name = Uri.parse(subtitlePath).lastPathSegment.orEmpty()
            return VideoTrackBean(name = name, type = TrackType.SUBTITLE, trackResource = subtitlePath)
        }

        fun danmu(danmu: LocalDanmuBean): VideoTrackBean {
            val name = Uri.parse(danmu.danmuPath).lastPathSegment.orEmpty()
            return VideoTrackBean(name = name, type = TrackType.DANMU, trackResource = danmu)
        }

        fun audio(audioPath: String): VideoTrackBean {
            val name = Uri.parse(audioPath).lastPathSegment.orEmpty()
            return VideoTrackBean(name = name, type = TrackType.AUDIO, trackResource = audioPath)
        }

        fun disable(type: TrackType, selected: Boolean): VideoTrackBean {
            return VideoTrackBean(name = "禁用", type = type, selected = selected, disable = true)
        }
    }
}