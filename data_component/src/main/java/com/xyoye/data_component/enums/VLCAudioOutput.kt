package com.xyoye.data_component.enums

/**
 * Created by xyoye on 2023/3/22
 */

enum class VLCAudioOutput(val value: String) {

    AUTO("audiotrack"),

    OPEN_SL_ES("opensles");

    companion object {
        fun valueOf(value: String?): VLCAudioOutput {
            return when (value) {
                "audiotrack" -> AUTO
                "opensles" -> OPEN_SL_ES
                else -> AUTO
            }
        }
    }
}