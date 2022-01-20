package com.xyoye.common_component.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by xyoye on 2020/7/7.
 */

open class BaseAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        //空布局ViewType
        const val VIEW_TYPE_EMPTY = -1

        //最大item类型数量
        private const val FALLBACK_DELEGATE_VIEW_TYPE = Int.MAX_VALUE - 1
    }

    //数据源
    var items: MutableList<Any> = mutableListOf()

    //是否已添加空布局
    private var isAddedEmptyView = false

    //空布局Holder建造者
    private lateinit var emptyViewHolderCreator: BaseViewHolderCreator<out ViewDataBinding>

    //viewHolder集合
    private val typeHolders: SparseArrayCompat<BaseViewHolderCreator<out ViewDataBinding>> = SparseArrayCompat()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolderCreator =
            if (viewType == VIEW_TYPE_EMPTY) getEmptyHolderCreator() else getHolderCreator(viewType)

        return BaseViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                viewHolderCreator.getResourceId(),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = if (items.size == 0 && isAddedEmptyView) 1 else items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        //绑定ViewHolder数据
        if (viewHolder.itemViewType == VIEW_TYPE_EMPTY) {
            getEmptyHolderCreator().apply {
                //绑定View
                registerItemView(viewHolder.itemView)
                //初始化item
                onBindViewHolder(null, -1, this)
            }
        } else {
            getHolderCreator(viewHolder.itemViewType).apply {
                //绑定View
                registerItemView(viewHolder.itemView)
                //初始化item
                val item = items[position]
                onBindViewHolder(item, position, this)
            }
        }
    }

    /**
     * 根据item下标，获取ViewHolder所在集合位置
     */
    override fun getItemViewType(position: Int): Int {
        //是否为空布局Item
        if (isEmptyPosition(position)) {
            return VIEW_TYPE_EMPTY
        }

        //只有一个ViewHolder时
        if (typeHolders.size() == 1) {
            return typeHolders.keyAt(0)
        }

        //多个ViewHolder时，需要代码确认
        for (i in 0 until typeHolders.size()) {
            val holder = typeHolders.valueAt(i)
            val data = items.getOrNull(position)
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

    private fun getEmptyHolderCreator(): BaseViewHolderCreator<out ViewDataBinding> {
        return emptyViewHolderCreator
    }

    /**
     * 当前Item是否为空布局
     */
    open fun isEmptyPosition(position: Int): Boolean {
        return position == 0 && isAddedEmptyView && items.size == 0
    }

    open fun addData(data: Any, position: Int = items.size) = apply {
        items.add(position, data)
        notifyItemInserted(position)
    }

    open fun addData(data: MutableList<Any>) = apply {
        items.addAll(data)
        notifyDataSetChanged()
    }

    open fun removeData(position: Int) = apply {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    open fun updateOneData(position: Int, newData: Any, payload: Boolean = true) {
        items[position] = newData
        if (payload) {
            notifyItemChanged(position, newData)
        } else {
            notifyItemChanged(position)
        }
    }

    open fun setData(list: List<Any>) {
        // TODO: 2022/1/20 这里希望用DiffUtil实现，但是由于空布局的存在，后面再整体修改
        items.apply {
            clear()
            addAll(list)
        }
        notifyDataSetChanged()
    }

    open fun getItem(position: Int): Any? = items.getOrNull(position)

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

    /**
     * 添加空布局的ViewHolder创建者
     */
    fun <V : ViewDataBinding> registerEmptyView(creator: BaseViewHolderCreator<V>) = apply {
        require(typeHolders.get(VIEW_TYPE_EMPTY) == null) {
            "duplicate addition of empty views is not allowed"
        }
        isAddedEmptyView = true
        emptyViewHolderCreator = creator
    }
}