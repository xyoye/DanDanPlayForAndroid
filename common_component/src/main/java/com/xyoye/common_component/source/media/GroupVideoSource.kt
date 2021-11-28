package com.xyoye.common_component.source.media

import com.xyoye.common_component.source.inter.GroupSource

/**
 * Created by xyoye on 2021/11/14.
 */

abstract class GroupVideoSource(
    private val index: Int,
    private val videoSources: List<*>
) : GroupSource {

    override fun getGroupIndex(): Int {
        return index
    }

    override fun getGroupSize(): Int {
        return videoSources.size
    }

    override fun hasNextSource(): Boolean {
        return index + 1 in videoSources.indices
    }

    override fun hasPreviousSource(): Boolean {
        return index - 1 in videoSources.indices
    }
}