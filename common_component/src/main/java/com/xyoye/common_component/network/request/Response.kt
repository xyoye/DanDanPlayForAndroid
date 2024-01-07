package com.xyoye.common_component.network.request

/**
 * Created by xyoye on 2024/1/5
 */

sealed class Response<T : Any> {
    data class Success<T : Any>(val data: T) : Response<T>()

    data class Error<T : Any>(val error: RequestError) : Response<T>()
}

val <T : Any> Response<T>.dataOrNull: T?
    get() = when (this) {
        is Response.Success -> data
        else -> null
    }

val <T : Any> Response<T>.errorOrNull: RequestError?
    get() = when (this) {
        is Response.Error -> error
        else -> null
    }