package com.xyoye.data_component.helper

import androidx.room.TypeConverter
import java.util.*

/**
 * Created by xyoye on 2020/7/7.
 *
 * 时间转换器
 */

open class DateConverter {
    @TypeConverter
    fun formTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}