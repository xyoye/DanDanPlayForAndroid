package com.xyoye.common_component.extension

import com.xyoye.common_component.config.AppConfig

/**
 * Created by xyoye on 2021/3/2.
 */

/**
 * 去重
 *
 * @param comparator 用于比较的集合
 * @params predicate 去重的条件
 */
inline fun <reified T, reified E> MutableIterator<T>.deduplication(
    comparator: Collection<E>,
    predicate: (T, E) -> Boolean
) {
    while (hasNext()) {
        val iterator = next()
        comparator.forEach {
            if (predicate.invoke(iterator, it)) {
                remove()
            }
        }
    }
}

/**
 * 过滤以.开头的文件
 */
inline fun <T> Iterable<T>.filterHiddenFile(predicate: (T) -> String): List<T> {
    return filterTo(ArrayList()) {
        AppConfig.isShowHiddenFile() || predicate.invoke(it).startsWith(".").not()
    }
}

/**
 * 从当前位置寻找上一个T类型的Item
 * @param currentIndex 当前位置
 * @param loop 到达开头时，返回结尾位置
 */
inline fun <reified T> List<*>.previousItemIndex(currentIndex: Int, loop: Boolean = true): Int {
    if (this.isEmpty()) {
        return -1
    }
    for (index in (currentIndex - 1) downTo 0) {
        if (this[index] is T) {
            return index
        }
    }

    if (loop) {
        return this.indexOfLast { it is T }
    }

    return -1
}


/**
 * 从当前位置寻找下一个T类型的Item
 * @param currentIndex 当前位置
 * @param loop 到达结尾时，返回开头位置
 */
inline fun <reified T> List<*>.nextItemIndex(currentIndex: Int, loop: Boolean = true): Int {
    if (this.isEmpty()) {
        return -1
    }
    for (index in (currentIndex + 1) until size) {
        if (this[index] is T) {
            return index
        }
    }

    if (loop) {
        return this.indexOfFirst { it is T }
    }

    return -1
}