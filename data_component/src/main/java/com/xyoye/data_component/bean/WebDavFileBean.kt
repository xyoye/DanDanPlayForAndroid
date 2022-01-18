package com.xyoye.data_component.bean

import java.net.URI


/**
 * Created by xyoye on 2022/1/17
 */
data class WebDavFileBean(
    val href: URI,
    val fileName: String,
    val danmuPath: String? = null,
    val subtitlePath: String? = null,
    val position: Long = 0,
    val duration: Long = 0,
    val uniqueKey: String? = null
)