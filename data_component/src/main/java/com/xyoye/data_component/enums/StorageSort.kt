package com.xyoye.data_component.enums

/**
 * Created by xyoye on 2023/3/31.
 */

enum class StorageSort(val value: Int) {
    NAME(0),
    SIZE(1);

    companion object {
        fun formValue(value: Int): StorageSort {
            return values().find { it.value == value } ?: NAME
        }
    }
}