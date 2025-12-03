package com.anjiu.repository.mmkv.processor.extension

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2024/4/3
 *    desc  :
 */
sealed class SupportedClassName(
    val className: ClassName,
    val mmkvPutFun: kotlin.String,
    val mmkvGetFun: kotlin.String,
    val nullable: kotlin.Boolean = true
) {

    data object String : SupportedClassName(kotlin.String::class.asClassName(), "putString", "getString")

    data object Int : SupportedClassName(kotlin.Int::class.asClassName(), "putInt", "getInt", false)

    data object Long : SupportedClassName(kotlin.Long::class.asClassName(), "putLong", "getLong", false)

    data object Float : SupportedClassName(kotlin.Float::class.asClassName(), "putFloat", "getFloat", false)

    data object Boolean : SupportedClassName(kotlin.Boolean::class.asClassName(), "putBoolean", "getBoolean", false)

    data object ByteArray : SupportedClassName(kotlin.ByteArray::class.asClassName(), "putBytes", "getBytes")

    data object StringSet : SupportedClassName(Set::class.asClassName(), "putStringSet", "getStringSet")

    companion object {

        fun get(ksType: KSType): SupportedClassName? {
            val compareClassName = ksType.toClassName().copy(
                nullable = false, annotations = emptyList(), tags = emptyMap()
            )
            return when {
                String.className == compareClassName -> String
                Int.className == compareClassName -> Int
                Long.className == compareClassName -> Long
                Float.className == compareClassName -> Float
                Boolean.className == compareClassName -> Boolean
                ByteArray.className == compareClassName -> ByteArray
                StringSet.className == compareClassName -> {
                    // Set仅支持String泛型
                    val argumentType = ksType.arguments.first().toTypeName()
                    val stringType = kotlin.String::class.asTypeName()
                    if (argumentType == stringType) {
                        StringSet
                    } else {
                        null
                    }
                }

                else -> null
            }
        }
    }
}