package com.xyoye.data_component.helper

import androidx.room.TypeConverter
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2021/1/18.
 */

open class MediaTypeConverter {
    @TypeConverter
    fun formValue(value: String): MediaType {
        return MediaType.fromValue(value)
    }

    @TypeConverter
    fun enumToValue(type: MediaType): String {
        return type.value
    }
}