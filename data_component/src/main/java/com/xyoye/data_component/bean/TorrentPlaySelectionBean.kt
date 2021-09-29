package com.xyoye.data_component.bean

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2021/9/29
 *     desc  :
 * </pre>
 */
data class TorrentPlaySelectionBean(
    val taskId: Long,
    val playUrl: String,
    val selectIndex: Int,
    val torrentPath: String
)