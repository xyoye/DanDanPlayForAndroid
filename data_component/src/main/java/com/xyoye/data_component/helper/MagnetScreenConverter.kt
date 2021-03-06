package com.xyoye.data_component.helper

import androidx.room.TypeConverter
import com.xyoye.data_component.enums.MagnetScreenType

/**
 * Created by xyoye on 2020/10/26.
 *
 * Magnet筛选类型转换器
 */

open class MagnetScreenConverter {
    @TypeConverter
    fun formValue(value: Int): MagnetScreenType {
        return MagnetScreenType.valueOf(value)
    }

    @TypeConverter
    fun enumToValue(type: MagnetScreenType): Int {
        return type.value
    }
}