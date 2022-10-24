package com.xyoye.common_component.extension

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

/**
 * Created by xyoye on 2022/9/19
 */

fun <T> CancellableContinuation<T>.resumeWhenAlive(value: T) {
    if (this.isActive) {
        resume(value)
    }
}