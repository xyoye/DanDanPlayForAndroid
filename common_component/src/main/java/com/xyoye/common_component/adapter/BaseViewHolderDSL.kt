package com.xyoye.common_component.adapter

import androidx.databinding.ViewDataBinding
import com.xyoye.mmkv_compiler.utils.throwException

/**
 * Created by xyoye on 2020/7/7.
 */

class BaseViewHolderDSL<T : Any, V : ViewDataBinding>(private val resourceId: Int) :
    BaseViewHolderCreator<T, V>() {
    private lateinit var viewType: ((data: T, position: Int) -> Boolean)
    private var viewHolder: (
        (data: T, position: Int, creator: BaseViewHolderCreator<T, out ViewDataBinding>) -> Unit
    )? = null
    private var emptyViewHolder: (() -> Unit)? = null

    override fun isForViewType(data: T?, position: Int): Boolean {
        if (!this::viewType.isInitialized)
            throwException("多类型Item需要类型判断：调用checkType")
        if (data == null)
            return false
        return viewType.invoke(data, position)
    }

    /**
     * 用于判断当前position对应数据类型
     */
    fun checkType(viewType: (data: T, position: Int) -> Boolean) {
        this.viewType = viewType
    }

    fun initView(holder: (data: T, position: Int, holder: BaseViewHolderCreator<T, out ViewDataBinding>) -> Unit) {
        this.viewHolder = holder
    }

    fun initEmptyView(holder: () -> Unit) {
        this.emptyViewHolder = holder
    }

    override fun getResourceId() = resourceId

    override fun onBindViewHolder(
        data: T?,
        position: Int,
        creator: BaseViewHolderCreator<T, out ViewDataBinding>
    ) {
        //空布局
        if (position == -1 && data == null) {
            emptyViewHolder?.invoke()
        }
        //正常布局不允许数据为null
        else if (data != null) {
            viewHolder?.invoke(data, position, creator)
        }
    }

}