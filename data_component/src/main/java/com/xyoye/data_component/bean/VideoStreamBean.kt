package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2020/11/16.
 */

data class VideoStreamBean(
    //流名称
    val trackName: String,

    //是否为音频流
    val isAudio: Boolean,

    //流ID
    val trackId: Int = -1,

    //是否被选中
    var isChecked: Boolean = false,

    //渲染器ID（exo）
    val renderId: Int = 0,

    //分组ID（exo）
    val trackGroupId: Int = 0,

    //是否为外挂流(字幕外挂流或禁用流)
    val isExternalStream: Boolean = false,

    //外挂流路径
    val externalStreamPath: String = "",

    // VLC流ID
    val vlcTrackId: String = "",
) {
    companion object {
        fun disableStream(isAudio: Boolean): VideoStreamBean {
            return VideoStreamBean(trackName = "Disable", isAudio = isAudio, isExternalStream = true)
        }
    }

    fun equalsIgnoreChecked(other: VideoStreamBean): Boolean {
        return this.copy(isChecked = false) == other.copy(isChecked = false)
    }
}