package com.xyoye.common_component.extension

import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2023/1/3
 */

val MediaType.deletable: Boolean
    get() = when (this) {
        MediaType.LOCAL_STORAGE,
        MediaType.STREAM_LINK,
        MediaType.MAGNET_LINK,
        MediaType.OTHER_STORAGE -> false
        else -> true
    }