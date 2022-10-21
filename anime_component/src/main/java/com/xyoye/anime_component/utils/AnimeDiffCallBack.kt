package com.xyoye.anime_component.utils

import androidx.recyclerview.widget.DiffUtil
import com.xyoye.data_component.data.AnimeData

/**
 * Created by xyoye on 2020/10/13.
 */

class AnimeDiffCallBack(
    private val oldData: MutableList<Any>,
    private val newData: MutableList<Any>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldData[oldItemPosition]
        val newItem = newData[newItemPosition]
        if (oldItem !is AnimeData)
            return false
        if (newItem !is AnimeData)
            return false
        return oldItem.animeId == newItem.animeId
    }

    override fun getOldListSize() = oldData.size

    override fun getNewListSize() = newData.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldData[oldItemPosition]
        val newItem = newData[newItemPosition]
        if (oldItem !is AnimeData)
            return false
        if (newItem !is AnimeData)
            return false
        return oldItem.animeId == newItem.animeId
    }

}