package com.xyoye.data_component.enums

/**
 * Created by xyoye on 2023/4/11
 */

enum class HistorySort(val value: Int) {
    TIME(0),
    NAME(1);

    companion object {
        fun formValue(value: Int): HistorySort {
            return values().find { it.value == value } ?: TIME
        }
    }
}