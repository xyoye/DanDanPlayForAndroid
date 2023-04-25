package com.xyoye.common_component.utils.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import com.xyoye.data_component.helper.moshi.NullToIntZero

object NullToIntZeroAdapter {

    @ToJson
    fun toJson(@NullToIntZero value: Int): String {
        return value.toString()
    }

    @FromJson
    @NullToIntZero
    fun fromJson(reader: JsonReader): Int {
        val result = if (reader.peek() === JsonReader.Token.NULL) {
            reader.nextNull()
        } else {
            reader.nextInt()
        }

        return result ?: 0
    }
}