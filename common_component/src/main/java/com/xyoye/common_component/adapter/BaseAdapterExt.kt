package com.xyoye.common_component.adapter

import androidx.databinding.ViewDataBinding
import com.xyoye.common_component.databinding.LayoutEmptyBinding

/**
 * Created by xyoye on 2020/7/7.
 */

fun buildAdapter(init: BaseAdapter.() -> Unit): BaseAdapter {
    return BaseAdapter().apply { init() }
}

inline fun <reified T : Any, V : ViewDataBinding> BaseAdapter.addItem(
    resourceId: Int,
    init: BaseViewHolderDSL<T, V>.() -> Unit
) {
    register(
        BaseViewHolderDSL<T, V>(resourceId, T::class).apply { init() }
    )
}

inline fun BaseAdapter.addEmptyView(
    resourceId: Int,
    init: (BaseViewHolderDSL<Any, LayoutEmptyBinding>.() -> Unit) = {}
) {
    val creator = BaseViewHolderDSL<Any, LayoutEmptyBinding>(resourceId, Any::class)
    creator.apply(init)
    registerEmptyView(creator)
}