package com.xyoye.data_component.helper

import androidx.room.TypeConverter

/**
 * Created by xyoye on 2021/1/18.
 */

open class BooleanConverter {
    @TypeConverter
    fun formBoolean(value: Boolean?): Int? {
        return if (value == null) null else if (value) 1 else 0
    }

    @TypeConverter
    fun intToBoolean(intValue: Int?): Boolean? {
        return intValue == 1
    }
}