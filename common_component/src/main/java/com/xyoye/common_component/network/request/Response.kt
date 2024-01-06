package com.xyoye.common_component.network.request

/**
 * Created by xyoye on 2024/1/5
 */

open class Response<T> {
    data class Success<T>(val data: T) : Response<T>()

    data class Error<T>(val error: RequestError) : Response<T>()
}

val <T> Response<T>.data: T?
    get() = when (this) {
        is Response.Success -> data
        else -> null
    }

val <T> Response<T>.error: RequestError?
    get() = when (this) {
        is Response.Error -> error
        else -> null
    }