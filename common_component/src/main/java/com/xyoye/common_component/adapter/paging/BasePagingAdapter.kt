package com.xyoye.common_component.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xyoye.common_component.adapter.BaseViewHolder
import com.xyoye.common_component.adapter.BaseViewHolderCreator

/**
 * Created by xyoye on 2020/12/2.
 */

class BasePagingAdapter<T : Any>(pagingItemCallback: PagingItemCallback<T> = PagingItemCallback.getDefault()) :
    PagingDataAdapter<T, RecyclerView.ViewHolder>(pagingItemCallback) {

    companion object {
        //最大item类型数量
        private const val FALLBACK_DELEGATE_VIEW_TYPE = Int.MAX_VALUE - 1
    }

    //viewHolder集合
    private val typeHolders: SparseArrayCompat<BaseViewHolderCreator<out ViewDataBinding>> = SparseArrayCompat()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        BaseViewHolder(
            DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(parent.context),
                getHolderCreator(viewType).getResourceId(),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position >= itemCount)
            return
        //绑定ViewHolder数据
        getHolderCreator(holder.itemViewType).apply {
            //绑定View
            initItemBinding(holder.itemView)
            //初始化item
            onBindViewHolder(getItem(position), position, this)
        }
    }

    /**
     * 根据item下标，获取ViewHolder所在集合位置
     */
    override fun getItemViewType(position: Int): Int {
        //只有一个ViewHolder时
        if (typeHolders.size() == 1) {
            return typeHolders.keyAt(0)
        }

        //多个ViewHolder时，需要代码确认
        for (i in 0 until typeHolders.size()) {
            val holder = typeHolders.valueAt(i)
            val data = getItem(position)
            if (holder.isForViewType(data, position)) {
                return typeHolders.keyAt(i)
            }
        }
        throw IllegalStateException("no holder added that matches at position: $position in data source")
    }

    /**
     * 根据viewType获取ViewHolder创建者
     */
    private fun getHolderCreator(viewType: Int): BaseViewHolderCreator<out ViewDataBinding> {
        return typeHolders.get(viewType)
            ?: throw RuntimeException("no holder added for view type: $viewType")
    }

    open fun submitPagingData(lifecycle: Lifecycle, pagingData: PagingData<T>) {
        submitData(lifecycle, pagingData)
    }

    /**
     * 添加一个ViewHolder创建者，以下标作为key保存
     */
    fun register(creator: BaseViewHolderCreator<out ViewDataBinding>) = apply {
        var viewType = typeHolders.size()
        while (typeHolders.get(viewType) != null) {
            viewType++
            require(viewType < FALLBACK_DELEGATE_VIEW_TYPE) {
                "the number of view type has reached the maximum limit"
            }
        }
        require(viewType < FALLBACK_DELEGATE_VIEW_TYPE) {
            "the number of view type has reached the maximum limit : $FALLBACK_DELEGATE_VIEW_TYPE"
        }
        typeHolders.put(viewType, creator)
    }

}