package com.xyoye.common_component.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.collection.contains
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
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

    @SuppressLint("NotifyDataSetChanged")
    override fun setData(data: List<Any>) {
        super.setData(data)
        // TODO: 2022/10/21 暂时无法使用DiffUtil做数据刷新，
        //  在List中仅修改数据内容时，无法进行刷新，因为修改与比较的都是同一个对象
        //  可尝试的方案：修改内容时对数据做深拷贝，修改后替换掉List中原数据

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