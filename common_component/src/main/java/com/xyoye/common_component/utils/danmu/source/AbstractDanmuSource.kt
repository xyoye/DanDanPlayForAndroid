package com.xyoye.common_component.utils.danmu.source

import com.xyoye.common_component.utils.danmu.helper.DanmuHashCalculator
import java.io.InputStream

/**
 * Created by xyoye on 2024/1/14.
 */

abstract class AbstractDanmuSource : DanmuSource {

    private var hash: String? = null

    override suspend fun hash(): String? {
        return hash ?: getStream()
            ?.use { DanmuHashCalculator.calculate(it) }
            .also { hash = it }
    }

    abstract suspend fun getStream(): InputStream?
}