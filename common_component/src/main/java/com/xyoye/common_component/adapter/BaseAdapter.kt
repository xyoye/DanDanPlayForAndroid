package com.xyoye.common_component.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.collection.contains
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by xyoye on 2020/7/7.
 */

class BaseAdapter : AnimatedAdapter<RecyclerView.ViewHolder>() {

    companion object {
        //空布局数据
        val EMPTY_ITEM = Any()

        //空布局ViewType
        const val VIEW_TYPE_EMPTY = -1

        //最大item类型数量
        private const val FALLBACK_DELEGATE_VIEW_TYPE = Int.MAX_VALUE - 1
    }

    //数据源
    val items: MutableList<Any> = mutableListOf()

    //数据差异比较器
    var diffCreator: AdapterDiffCreator? = AdapterDiffCreator()

    //viewHolder集合
    private val typeHolders = SparseArrayCompat<BaseViewHolderCreator<out ViewDataBinding>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BaseViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                getHolderCreator(viewType).getResourceId(),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        bindViewHolderAnimation(viewHolder)
        getHolderCreator(viewHolder.itemViewType).apply {
            initItemBinding(viewHolder.itemView)
            onBindViewHolder(items[position], position, this)
        }
    }

    /**
     * 根据item下标，获取ViewHolder所在集合位置
     */
    override fun getItemViewType(position: Int): Int {
        if (items[position] == EMPTY_ITEM
            && typeHolders.contains(VIEW_TYPE_EMPTY)
        ) {
            return VIEW_TYPE_EMPTY
        }

        //只有一个ViewHolder时
        if (typeHolders.size() == 1) {
            return typeHolders.keyAt(0)
        }

        //多个ViewHolder时，需要代码确认
        for (i in 0 until typeHolders.size()) {
            if (typeHolders.keyAt(i) == VIEW_TYPE_EMPTY) {
                continue
            }

            val holder = typeHolders.valueAt(i)
            if (holder.isForViewType(items[position], position)) {
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

    override fun setData(data: List<Any>) {
        super.setData(data)

        if (diffCreator != null) {
            setDiffData(data, diffCreator!!)
        } else {
            setNotifyData(data)
        }
    }

    /**
     * 使用DiffUtil刷新数据
     */
    private fun setDiffData(data: List<Any>, diffCreator: AdapterDiffCreator) {
        val newItems = data.map { diffCreator.createNewData(it) }.toMutableList()
        //数据为空时，显示空布局
        if (newItems.isEmpty() && typeHolders.contains(VIEW_TYPE_EMPTY)) {
            newItems.add(EMPTY_ITEM)
        }
        val diffCallBack = AdapterDiffCallBack(items, newItems, diffCreator)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * 使用notifyDataSetChanged刷新数据
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun setNotifyData(data: List<Any>) {
        items.clear()
        items.addAll(data)

        //数据为空时，显示空布局
        if (items.isEmpty() && typeHolders.contains(VIEW_TYPE_EMPTY)) {
            items.add(EMPTY_ITEM)
        }
        notifyDataSetChanged()
    }

    /**
     * 添加一个ViewHolder创建者，以下标作为key保存
     */
    fun register(creator: BaseViewHolderCreator<out ViewDataBinding>, customViewType: Int? = null) =
        apply {
            var viewType = customViewType ?: typeHolders.size()
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