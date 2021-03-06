package com.xyoye.common_component.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xyoye.common_component.network.request.RequestError
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.helper.Loading

/**
 * Created by xyoye on 2020/4/13.
 */

abstract class BaseViewModel : ViewModel() {
    val loadingObserver: MutableLiveData<Pair<Int, String?>> = MutableLiveData()

    protected fun showLoading(msg: String) {
        loadingObserver.postValue(Pair(Loading.SHOW_LOADING_MSG, msg))
    }

    protected fun showLoading() {
        loadingObserver.postValue(Pair(Loading.SHOW_LOADING, null))
    }

    protected fun hideLoading() {
        loadingObserver.postValue(Pair(Loading.HIDE_LOADING, null))
    }

    protected fun hideLoadingSuccess() {
        loadingObserver.postValue(Pair(Loading.HIDE_LOADING_SUCCESS, null))
    }

    protected fun hideLoadingFailed() {
        loadingObserver.postValue(Pair(Loading.HIDE_LOADING_FAILED, null))
    }

    protected fun showNetworkError(error: RequestError) {
        ToastCenter.showError("x${error.code} ${error.msg}")
    }
}