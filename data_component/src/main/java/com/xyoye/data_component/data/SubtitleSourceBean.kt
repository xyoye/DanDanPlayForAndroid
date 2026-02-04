package com.xyoye.data_component.data

import kotlinx.serialization.Serializable

/**
 * Created by xyoye on 2020/12/1.
 */

@Serializable
data class SubtitleSourceBean(
    val id: Int = 0,
    val name: String? = null,
    val time: String? = null,
    val type: String? = null,
    val language: String? = null,

    val isMatch: Boolean = false,
    val matchUrl: String = "",
    val source: String = ""
)