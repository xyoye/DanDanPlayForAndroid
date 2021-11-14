package com.xyoye.common_component.source

import com.xyoye.common_component.source.inter.ExtraSource
import com.xyoye.common_component.source.inter.GroupSource
import com.xyoye.common_component.source.inter.VideoSource

/**
 * Created by xyoye on 2021/11/14.
 */

abstract class MediaSource(
    private val index: Int,
    private val playGroup: List<*>
) : VideoSource, ExtraSource, GroupSource {

    override fun getGroupIndex(): Int {
        return index
    }

    override fun getGroupSize(): Int {
        return playGroup.size
    }

    override fun hasNextSource(): Boolean {
        return index + 1 in playGroup.indices
    }

    override fun hasPreviousSource(): Boolean {
        return index - 1 in playGroup.indices
    }

    override suspend fun nextSource(): MediaSource? {
        return indexSource(index + 1)
    }

    override suspend fun previousSource(): MediaSource? {
        return indexSource(index - 1)
    }
}