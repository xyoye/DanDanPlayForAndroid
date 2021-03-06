package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2020/11/16.
 */

data class VideoTrackBean(
    //流名称
    val trackName: String,

    //是否为音频流
    val isAudio: Boolean,

    //流ID
    val trackId: Int,

    //是否被选中
    var isChecked: Boolean,

    //渲染器ID（exo）
    val renderId: Int = 0,

    //分组ID（exo）
    val trackGroupId: Int = 0,

    //外挂字幕路径
    val mSourceUrl: String? = null,

    //是否为外挂字幕
    val isExTrack: Boolean = false,

    //当前流是否被禁用
    val isDisable: Boolean = false
)