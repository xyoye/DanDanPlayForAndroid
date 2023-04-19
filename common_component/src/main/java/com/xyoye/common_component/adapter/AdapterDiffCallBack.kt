package com.xyoye.common_component.adapter

import androidx.recyclerview.widget.DiffUtil


/**
 * Created by xyoye on 2022/1/20
 */
class AdapterDiffCallBack(
    private val oldData: List<Any>,
    private val newData: List<Any>,
    private val diffCreator: AdapterDiffCreator
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldData.size

    override fun getNewListSize() = newData.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return diffCreator.isSameItem(oldData[oldItemPosition], newData[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return diffCreator.isSameContent(oldData[oldItemPosition], newData[newItemPosition])
    }
}