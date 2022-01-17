package com.xyoye.stream_component.ui.activities.web_dav_file

import com.xyoye.sardine.DavResource


/**
 * Created by xyoye on 2022/1/17
 */
data class WebDavFileBean(
    val davSource: DavResource,
    val danmuPath: String? = null,
    val subtitlePath: String? = null,
    val uniqueKey: String? = null
)