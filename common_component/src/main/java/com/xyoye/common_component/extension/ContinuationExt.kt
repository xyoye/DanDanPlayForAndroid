package com.xyoye.common_component.extension

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2022/9/19
 *     desc  :
 * </pre>
 */

fun <T> CancellableContinuation<T>.resumeWhenAlive(value: T) {
    if (this.isActive) {
        resume(value)
    }
}