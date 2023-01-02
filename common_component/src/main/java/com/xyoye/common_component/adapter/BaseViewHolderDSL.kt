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
    private var checkViewType2: ((data: Any) -> Boolean)? = null

    private var viewHolder: (
        (data: T, position: Int, creator: BaseViewHolderCreator<out ViewDataBinding>) -> Unit
    )? = null
    private var viewHolder2: ((data: T, position: Int) -> Unit)? = null
    private var viewHolder3: ((data: T) -> Unit)? = null

    private var emptyViewHolder: (() -> Unit)? = null

    override fun isForViewType(data: Any?, position: Int): Boolean {
        if (data == null)
            return false

        return checkViewType?.invoke(data, position)
            ?: checkViewType2?.invoke(data)
            ?: clazz.isInstance(data)
    }

    /**
     * 用于判断当前position对应数据类型
     */
    fun checkType(viewType: (data: Any, position: Int) -> Boolean) {
        this.checkViewType = viewType
    }

    fun checkType(viewType: (data: Any) -> Boolean) {
        this.checkViewType2 = viewType
    }

    fun initView(holder: (data: T, position: Int, holder: BaseViewHolderCreator<out ViewDataBinding>) -> Unit) {
        this.viewHolder = holder
    }

    fun initView(holder: (data: T, position: Int) -> Unit) {
        this.viewHolder2 = holder
    }

    fun initView(holder: (data: T) -> Unit) {
        this.viewHolder3 = holder
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
        data ?: return

        //空布局
        if (data == BaseAdapter.EMPTY_ITEM) {
            emptyViewHolder?.invoke()
            return
        }

        viewHolder?.invoke(data as T, position, creator)
            ?: viewHolder2?.invoke(data as T, position)
            ?: viewHolder3?.invoke(data as T)
    }

}