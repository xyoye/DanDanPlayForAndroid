package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2021/1/4.
 */

data class DownloadSelectionBean(
    val name: String?,

    val size: Long,

    var selected: Boolean = true
)