package com.xyoye.common_component.adapter.paging

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

/**
 * Created by xyoye on 2020/12/4.
 */

class PagingFooterAdapter(private val retryCallback: () -> Unit) :
    LoadStateAdapter<PagingFooterViewHolder>() {
    override fun onBindViewHolder(holder: PagingFooterViewHolder, loadState: LoadState) {
        holder.setState(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ) = PagingFooterViewHolder(parent, retryCallback)
}