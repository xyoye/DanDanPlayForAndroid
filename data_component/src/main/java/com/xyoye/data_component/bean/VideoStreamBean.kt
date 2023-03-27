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

    //是否为外挂流
    val isExternalStream: Boolean = false,

    //外挂流路径
    val externalStreamPath: String = ""
) {
    companion object {
        //音频禁用流
        private val DISABLE_AUDIO = VideoStreamBean(trackName = "Disable", isAudio = true, isExternalStream = true)

        //字幕禁用流
        private val DISABLE_SUBTITLE = VideoStreamBean(trackName = "Disable", isAudio = false, isExternalStream = true)

        fun disableStream(isAudio: Boolean): VideoStreamBean {
            return if (isAudio) DISABLE_AUDIO else DISABLE_SUBTITLE
        }
    }
}