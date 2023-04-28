package com.xyoye.common_component.utils.comparator

import java.text.CollationKey
import java.text.Collator

/**
 * Created by xyoye on 2023/3/31.
 */

class FileSizeComparator<T>(
    private val getName: (T) -> String,
    private val getSize: (T) -> Long,
    private val isDirectory: (T) -> Boolean,
    private val asc: Boolean = true,
    private val directoryFirst: Boolean = true
) : Comparator<T> {
    override fun compare(o1: T, o2: T): Int {
        if (o1 == null) {
            return if (asc) -1 else 1
        }
        if (o2 == null) {
            return if (asc) 1 else -1
        }

        val isDirectory1 = directory(o1, o2, true)
        val isDirectory2 = directory(o1, o2, false)

        return when {
            isDirectory1 == isDirectory2 -> compareFileSize(o1, o2)
            isDirectory1 -> -1
            else -> 1
        }
    }

    private fun compareFileSize(o1: T, o2: T): Int {
        val size1 = size(o1, o2, true)
        val size2 = size(o1, o2, false)
        val result = size1.compareTo(size2)
        if (result == 0) {
            return compareFileName(o1, o2)
        }
        return result
    }

    private fun compareFileName(o1: T, o2: T): Int {
        val key1 = nameKey(o1, o2, true)
        val key2 = nameKey(o1, o2, false)
        return key1.compareTo(key2)
    }

    private fun nameKey(o1: T, o2: T, first: Boolean): CollationKey {
        return if (first) {
            if (asc) o1 else o2
        } else {
            if (asc) o2 else o1
        }.run(getName).run {
            Collator.getInstance().getCollateKey(this)
        }
    }

    private fun size(o1: T, o2: T, first: Boolean): Long {
        return if (first) {
            if (asc) o1 else o2
        } else {
            if (asc) o2 else o1
        }.run(getSize)
    }

    private fun directory(o1: T, o2: T, first: Boolean): Boolean {
        return if (first) {
            if (directoryFirst) o1 else o2
        } else {
            if (directoryFirst) o2 else o1
        }.run(isDirectory)
    }
}