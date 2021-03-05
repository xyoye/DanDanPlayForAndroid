package com.xyoye.common_component.adapter.paging

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.paging.PagingData
import com.xyoye.common_component.adapter.BaseViewHolderDSL

/**
 * Created by xyoye on 2020/7/7.
 */

fun <T : Any> buildPagingAdapter(init: BasePagingAdapter<T>.() -> Unit): BasePagingAdapter<T> {
    return BasePagingAdapter<T>().apply { init() }
}

fun <T : Any> BasePagingAdapter<T>.initData(lifecycle: Lifecycle, pagingData: PagingData<T>) {
    submitPagingData(lifecycle, pagingData)
}

fun <T : Any, V: ViewDataBinding> BasePagingAdapter<T>.addItem(resourceId: Int, init: BaseViewHolderDSL<T, V>.() -> Unit) {
    register(
        BaseViewHolderDSL<T, V>(resourceId).apply { init() }
    )
}