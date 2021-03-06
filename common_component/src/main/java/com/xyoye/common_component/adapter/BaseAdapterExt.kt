package com.xyoye.common_component.adapter

import androidx.databinding.ViewDataBinding
import com.xyoye.common_component.databinding.LayoutEmptyBinding

/**
 * Created by xyoye on 2020/7/7.
 */

fun <T : Any> buildAdapter(init: BaseAdapter<T>.() -> Unit): BaseAdapter<T> {
    return BaseAdapter<T>().apply { init() }
}

fun <T : Any> BaseAdapter<T>.initData(items: MutableList<T>) {
    this.items = items
}

fun <T : Any, V : ViewDataBinding> BaseAdapter<T>.addItem(
    resourceId: Int,
    init: BaseViewHolderDSL<T, V>.() -> Unit
) {
    register(
        BaseViewHolderDSL<T, V>(resourceId).apply { init() }
    )
}

fun <T : Any> BaseAdapter<T>.addEmptyView(
    resourceId: Int,
    init: (BaseViewHolderDSL<T, LayoutEmptyBinding>.() -> Unit)? = null
) {
    val creator = BaseViewHolderDSL<T, LayoutEmptyBinding>(resourceId)
    init?.let {
        creator.apply(it)
    }
    registerEmptyView(creator)
}