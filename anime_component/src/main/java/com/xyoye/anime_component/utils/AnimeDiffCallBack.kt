package com.xyoye.anime_component.utils

import androidx.recyclerview.widget.DiffUtil
import com.xyoye.anime_component.databinding.ItemAnimeBinding
import com.xyoye.common_component.adapter.BaseAdapter
import com.xyoye.data_component.data.AnimeData

/**
 * Created by xyoye on 2020/10/13.
 */

class AnimeDiffCallBack(
    private val oldData: MutableList<AnimeData>,
    private val newData: MutableList<AnimeData>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldData[oldItemPosition].animeId == newData[newItemPosition].animeId

    override fun getOldListSize() = oldData.size

    override fun getNewListSize() = newData.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldData[oldItemPosition].animeId == newData[newItemPosition].animeId

}