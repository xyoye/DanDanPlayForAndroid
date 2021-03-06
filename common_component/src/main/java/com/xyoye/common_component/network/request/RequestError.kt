package com.xyoye.common_component.network.request

data class RequestError(val code: Int, val msg: String): Throwable()