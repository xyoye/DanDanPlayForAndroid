package com.xyoye.local_component.utils

import androidx.recyclerview.widget.DiffUtil
import com.xyoye.data_component.entity.PlayHistoryEntity

/**
 * Created by xyoye on 2023/1/7.
 */

class HistoryDiffCallback(
    private val oldData: List<Any>,
    private val newData: List<PlayHistoryEntity>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldData.size

    override fun getNewListSize() = newData.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldData[oldItemPosition]
        if (oldItem !is PlayHistoryEntity)
            return false

        val newItem = newData[newItemPosition]
        return oldItem.uniqueKey == newItem.uniqueKey && oldItem.storageId == newItem.storageId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldData[oldItemPosition]
        if (oldItem !is PlayHistoryEntity)
            return false

        val newItem = newData[newItemPosition]
        return oldItem == newItem
    }
}