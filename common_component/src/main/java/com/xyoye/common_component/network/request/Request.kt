package com.xyoye.common_component.network.request

import com.xyoye.data_component.data.CommonJsonData
import kotlinx.coroutines.*

/**
 * Created by xyoye on 2020/7/6.
 */

class Request<T> {
    lateinit var api: suspend () -> T

    private var onStart: (() -> Unit)? = null
    private var onSuccess: ((T) -> Unit)? = null
    private var onError: ((RequestError) -> Unit)? = null
    private var onComplete: (() -> Unit)? = null

    infix fun api(api: suspend () -> T) {
        this.api = api
    }

    infix fun onStart(start: () -> Unit) {
        this.onStart = start
    }

    infix fun onSuccess(success: (T) -> Unit) {
        this.onSuccess = success
    }

    infix fun onError(error: (RequestError) -> Unit) {
        this.onError = error
    }

    infix fun onComplete(complete: () -> Unit) {
        this.onComplete = complete
    }

    fun doRequest(scope: CoroutineScope) {
        scope.launch(context = Dispatchers.Main) {
            onStart?.invoke()

            try {
                val deferred = scope.async(Dispatchers.IO, start = CoroutineStart.LAZY) {
                    api()
                }

                val result = deferred.await()

                if (result is CommonJsonData && !result.success) {
                    onError?.invoke(RequestErrorHandler.handleCommonError(result))
                } else {
                    onSuccess?.invoke(result)
                }
            } catch (e: Exception) {
                onError?.invoke(
                    RequestErrorHandler(e).handlerError()
                )
            } finally {
                onComplete?.invoke()
            }
        }
    }
}

inline fun <T> httpRequest(scope: CoroutineScope, requester: Request<T>.() -> Unit) {
    Request<T>().apply(requester).doRequest(scope)
}