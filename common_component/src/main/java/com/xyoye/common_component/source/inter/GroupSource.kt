package com.xyoye.common_component.source.inter

/**
 * Created by xyoye on 2021/11/14.
 *
 * 组资源，实现资源间切换
 */

interface GroupSource: VideoSource {
    fun getGroupIndex(): Int

    fun getGroupSize(): Int

    fun hasNextSource(): Boolean

    fun hasPreviousSource(): Boolean

    suspend fun indexSource(index: Int): GroupSource?

    suspend fun nextSource(): GroupSource?

    suspend fun previousSource(): GroupSource?
}