package com.xyoye.stream_component.utils.storage

import androidx.recyclerview.widget.DiffUtil
import com.xyoye.common_component.storage.file.StorageFile

/**
 * Created by xyoye on 2023/1/7.
 */

class StorageFileDiffCallback(
    private val oldData: List<Any>,
    private val newData: List<StorageFile>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldData.size

    override fun getNewListSize() = newData.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldData[oldItemPosition]
        if (oldItem !is StorageFile)
            return false

        val newItem = newData[newItemPosition]
        return oldItem.uniqueKey() == newItem.uniqueKey()
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldData[oldItemPosition]
        if (oldItem !is StorageFile)
            return false

        val newItem = newData[newItemPosition]
        return oldItem.fileUrl() == newItem.fileUrl()
                && oldItem.playHistory == newItem.playHistory
    }
}