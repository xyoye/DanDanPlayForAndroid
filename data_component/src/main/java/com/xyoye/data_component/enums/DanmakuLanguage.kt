package com.xyoye.data_component.enums

/**
 * Created by xyoye on 2023/5/27.
 */

enum class DanmakuLanguage(val value: Int) {
    ORIGINAL(0),

    SC(1),

    TC(2);

    companion object {
        fun formValue(value: Int): DanmakuLanguage {
            return values().find { it.value == value } ?: ORIGINAL
        }
    }
}