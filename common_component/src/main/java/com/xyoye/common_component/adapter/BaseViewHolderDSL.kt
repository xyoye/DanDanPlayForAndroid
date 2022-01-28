package com.xyoye.common_component.adapter

import androidx.databinding.ViewDataBinding
import kotlin.reflect.KClass

/**
 * Created by xyoye on 2020/7/7.
 */

class BaseViewHolderDSL<T : Any, V : ViewDataBinding>(
    private val resourceId: Int,
    private val clazz: KClass<T>
) : BaseViewHolderCreator<V>() {
    private var checkViewType: ((data: Any, position: Int) -> Boolean)? = null
    private var viewHolder: (
        (data: T, position: Int, creator: BaseViewHolderCreator<out ViewDataBinding>) -> Unit
    )? = null
    private var emptyViewHolder: (() -> Unit)? = null

    override fun isForViewType(data: Any?, position: Int): Boolean {
        if (data == null)
            return false
        if (checkViewType != null) {
            return checkViewType!!.invoke(data, position)
        }
        return clazz.isInstance(data)
    }

    /**
     * 用于判断当前position对应数据类型
     */
    fun checkType(viewType: (data: Any, position: Int) -> Boolean) {
        this.checkViewType = viewType
    }

    fun initView(holder: (data: T, position: Int, holder: BaseViewHolderCreator<out ViewDataBinding>) -> Unit) {
        this.viewHolder = holder
    }

    fun initEmptyView(holder: () -> Unit) {
        this.emptyViewHolder = holder
    }

    override fun getResourceId() = resourceId

    override fun onBindViewHolder(
        data: Any?,
        position: Int,
        creator: BaseViewHolderCreator<out ViewDataBinding>
    ) {
        //空布局
        if (position == -1 && data == null) {
            emptyViewHolder?.invoke()
            return
        }

        data ?: return
        viewHolder?.invoke(data as T, position, creator)
    }

}