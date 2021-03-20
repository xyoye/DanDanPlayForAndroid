package com.xyoye.common_component.extension

import java.io.File

/**
 * Created by xyoye on 2021/3/20.
 */

fun String?.toFile() : File? {
    if (this.isNullOrEmpty())
        return null
    return File(this)
}