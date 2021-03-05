package com.xyoye.common_component.adapter.paging

import androidx.recyclerview.widget.DiffUtil

/**
 * Created by xyoye on 2020/12/2.
 */

class PagingItemCallback<T>(
    private val mAreItemsTheSame: (T, T) -> Boolean = { o, n -> o == n },
    private val mAreContentsTheSame: (T, T) -> Boolean = { o, n -> o == n }
) : DiffUtil.ItemCallback<T>() {

    companion object {
        fun <T> getDefault(): PagingItemCallback<T> {
            return PagingItemCallback()
        }
    }

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return mAreItemsTheSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return mAreContentsTheSame(oldItem, newItem)
    }

}