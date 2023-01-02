package com.xyoye.data_component.bean

/**
 * Created by xyoye on 2023/1/1.
 */

data class StorageFilePath(
    val name: String,
    val route: String,
    var isLast: Boolean = false
) {
    override fun hashCode(): Int {
        return arrayOf(name, route).contentDeepHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as StorageFilePath
        return name == other.name
                && route == other.route
    }
}