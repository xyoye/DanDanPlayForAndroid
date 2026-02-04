package com.xyoye.common_component.utils

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Created by xyoye on 2021/3/26.
 */

object JsonHelper {

    val JSON: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        coerceInputValues = true
    }

    inline fun <reified T> parseJson(jsonStr: String): T? {
        if (jsonStr.isEmpty())
            return null

        try {
            return JSON.decodeFromString(jsonStr)
        } catch (e: SerializationException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return null
    }

    inline fun <reified T> parseJsonList(jsonStr: String): List<T> {
        if (jsonStr.isEmpty())
            return emptyList()

        try {
            return JSON.decodeFromString(jsonStr)
        } catch (e: SerializationException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return emptyList()
    }

    fun parseJsonMap(jsonStr: String): Map<String, String> {
        if (jsonStr.isEmpty())
            return emptyMap()

        try {
            return JSON.decodeFromString(jsonStr)
        } catch (e: SerializationException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        return emptyMap()
    }

    inline fun <reified T> toJson(t: T?): String? {
        t ?: return null

        try {
            return JSON.encodeToString(t)
        } catch (e: SerializationException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return null
    }
}
