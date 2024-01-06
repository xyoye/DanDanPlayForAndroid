package com.xyoye.common_component.network.repository

import com.xyoye.common_component.network.request.Request

/**
 * Created by xyoye on 2024/1/6.
 */

open class BaseRepository {

    fun request() = Request()
}