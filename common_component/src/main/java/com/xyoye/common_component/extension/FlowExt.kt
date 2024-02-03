package com.xyoye.common_component.extension

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by xyoye on 2023/3/19.
 */

/**
 * 关联生命周期的Flow收集扩展方法，关注生命周期状态
 * 在生命周期为[Lifecycle.State.STARTED]时开始收集
 * 在生命周期为[Lifecycle.State.DESTROYED]时取消收集
 */
inline fun <T> Flow<T>.collectAtStarted(
    owner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) = owner.lifecycleScope.launch {
    owner.repeatOnLifecycle(minActiveState) {
        collectLatest {
            action(it)
        }
    }
}

/**
 * 关联生命周期的Flow收集扩展方法，不关注生命周期状态
 */
inline fun <T> Flow<T>.collectAtLaunch(
    owner: LifecycleOwner,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) = owner.lifecycleScope.launch {
    collectLatest {
        action(it)
    }
}

val <T> MutableSharedFlow<T>.collectable: SharedFlow<T> get() = this

val <T> MutableStateFlow<T>.collectable: StateFlow<T> get() = this