package com.xyoye.player.wrapper

import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.enums.TrackType

/**
 * Created by xyoye on 2020/11/1.
 */

interface InterVideoTrack {

    /**
     * 是否支持添加轨道
     */
    fun supportAddTrack(type: TrackType): Boolean

    /**
     * 添加轨道
     */
    fun addTrack(track: VideoTrackBean): Boolean

    /**
     * 获取轨道
     */
    fun getTracks(type: TrackType): List<VideoTrackBean>

    /**
     * 选中轨道
     */
    fun selectTrack(track: VideoTrackBean)

    /**
     * 取消选中轨道
     */
    fun deselectTrack(type: TrackType)
}