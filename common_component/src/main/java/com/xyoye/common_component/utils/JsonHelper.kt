package com.xyoye.common_component.utils

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.xyoye.common_component.utils.moshi.NullToEmptyStringAdapter
import com.xyoye.common_component.utils.moshi.NullToIntZeroAdapter
import com.xyoye.common_component.utils.moshi.NullToLongZeroAdapter
import java.io.IOException

/**
 * Created by xyoye on 2021/3/26.
 */

object JsonHelper {

    val MO_SHI: Moshi = Moshi.Builder()
        .add(NullToEmptyStringAdapter)
        .add(NullToLongZeroAdapter)
        .add(NullToIntZeroAdapter)
        .build()

    inline fun <reified T> parseJson(jsonStr: String): T? {
        if (jsonStr.isEmpty())
            return null

        try {
            val jsonAdapter = MO_SHI.adapter(T::class.java)
            return jsonAdapter.fromJson(jsonStr)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JsonDataException) {
            e.printStackTrace()
        }
        return null
    }

    inline fun <reified T> parseJsonList(jsonStr: String): List<T> {
        if (jsonStr.isEmpty())
            return emptyList()

        try {
            val type = Types.newParameterizedType(List::class.java, T::class.java)
            val adapter = MO_SHI.adapter<List<T>>(type)
            return adapter.fromJson(jsonStr) ?: emptyList()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JsonDataException) {
            e.printStackTrace()
        }
        return emptyList()
    }

    fun parseJsonMap(jsonStr: String): Map<String, String> {
        if (jsonStr.isEmpty())
            return emptyMap()

        try {
            val type =
                Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            val adapter = MO_SHI.adapter<Map<String, String>>(type)
            return adapter.fromJson(jsonStr) ?: emptyMap()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JsonDataException) {
            e.printStackTrace()
        }

        return emptyMap()
    }

    inline fun <reified T> toJson(t: T?): String? {
        t ?: return null

        try {
            val adapter = MO_SHI.adapter(T::class.java)
            return adapter.toJson(t)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JsonDataException) {
            e.printStackTrace()
        }
        return null
    }
}