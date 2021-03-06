package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2021/1/11.
 */

data class EditBean(
    val title: String,
    val emptyWarningMsg: String,
    val hint: String,
    val defaultText: String? = null
)