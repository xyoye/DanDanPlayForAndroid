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
 */
inline fun <reified T> List<*>.previousItemIndex(currentIndex: Int): Int {
    return findIndexOnLeft(currentIndex, loop = true) {
        it is T
    }
}


/**
 * 从当前位置寻找下一个T类型的Item
 * @param currentIndex 当前位置
 */
inline fun <reified T> List<*>.nextItemIndex(currentIndex: Int): Int {
    return findIndexOnRight(currentIndex, loop = true) {
        it is T
    }
}


/**
 * 从当前位置往左寻找目标
 * @param from 当前位置
 * @param loop 左边无目标时，返回右边最后一个索引
 */
fun <T> List<T>.findIndexOnLeft(
    from: Int,
    loop: Boolean = false,
    predicate: (T) -> Boolean
): Int {
    if (this.isEmpty()) {
        return -1
    }

    for (index in from - 1 downTo 0) {
        if (predicate.invoke(this[index])) {
            return index
        }
    }

    if (loop) {
        return this.indexOfLast { predicate.invoke(it) }
    }
    return -1
}

/**
 * 从当前位置的往右寻找目标
 * @param from 当前位置
 * @param loop 右边无目标时，返回左边第一个索引
 */
fun <T> List<T>.findIndexOnRight(
    from: Int,
    loop: Boolean = false,
    predicate: (T) -> Boolean
): Int {
    if (this.isEmpty()) {
        return -1
    }

    for (index in from + 1 until size) {
        if (predicate.invoke(this[index])) {
            return index
        }
    }

    if (loop) {
        return this.indexOfFirst { predicate.invoke(it) }
    }
    return -1
}