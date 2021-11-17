package com.xyoye.data_component.bean


/**
 * Created by xyoye on 2021/11/16.
 */

data class TorrentPlaySelectionBean(
    val taskId: Long,
    val playUrl: String,
    val selectIndex: Int,
    val torrentPath: String
)