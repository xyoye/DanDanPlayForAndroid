package com.xyoye.player.utils

/**
 * Created by xyoye on 2021/11/14.
 */

interface PlaySourceListener {

    fun hasNextSource(): Boolean

    fun hasPreviousSource(): Boolean

    fun nextSource()

    fun previousSource()
}