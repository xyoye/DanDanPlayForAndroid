package com.xyoye.common_component.extension

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