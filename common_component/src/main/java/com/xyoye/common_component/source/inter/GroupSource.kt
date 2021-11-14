package com.xyoye.common_component.source.inter

import com.xyoye.common_component.source.MediaSource

/**
 * Created by xyoye on 2021/11/14.
 */

interface GroupSource {
    fun getGroupIndex(): Int

    fun getGroupSize(): Int

    fun hasNextSource(): Boolean

    fun hasPreviousSource(): Boolean

    suspend fun indexSource(index: Int): MediaSource?

    suspend fun nextSource(): MediaSource?

    suspend fun previousSource(): MediaSource?
}