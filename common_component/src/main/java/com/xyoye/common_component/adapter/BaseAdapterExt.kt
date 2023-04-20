package com.xyoye.common_component.adapter

import androidx.databinding.ViewDataBinding
import com.xyoye.common_component.databinding.LayoutEmptyBinding

/**
 * Created by xyoye on 2020/7/7.
 */

fun buildAdapter(init: BaseAdapter.() -> Unit): BaseAdapter {
    return BaseAdapter().apply { init() }
}

fun BaseAdapter.setupDiffUtil(init: AdapterDiffCreator.() -> Unit) {
    this.diffCreator = AdapterDiffCreator().apply { init() }
}

fun BaseAdapter.setupVerticalAnimation() {
    setAnimationType(AnimatedAdapter.AnimationType.VERTICAL)
}

fun BaseAdapter.setupHorizontalAnimation() {
    setAnimationType(AnimatedAdapter.AnimationType.HORIZONTAL)
}

fun BaseAdapter.disableDiffUtil() {
    this.diffCreator = null
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
    register(
        BaseViewHolderDSL<Any, LayoutEmptyBinding>(resourceId, Any::class).apply { init() },
        customViewType = BaseAdapter.VIEW_TYPE_EMPTY
    )

    setData(listOf(BaseAdapter.EMPTY_ITEM))
}