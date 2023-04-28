package com.xyoye.common_component.utils.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import com.xyoye.data_component.helper.moshi.NullToLongZero

object NullToLongZeroAdapter {

    @ToJson
    fun toJson(@NullToLongZero value: Long): String {
        return value.toString()
    }

    @FromJson
    @NullToLongZero
    fun fromJson(reader: JsonReader): Long {
        val result = if (reader.peek() === JsonReader.Token.NULL) {
            reader.nextNull()
        } else {
            reader.nextLong()
        }

        return result ?: 0L
    }
}