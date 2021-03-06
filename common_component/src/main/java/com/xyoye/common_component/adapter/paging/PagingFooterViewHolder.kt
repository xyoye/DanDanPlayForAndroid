package com.xyoye.common_component.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.xyoye.common_component.R
import com.xyoye.common_component.databinding.ItemPagingFooterBinding
import com.xyoye.common_component.network.request.RequestError

/**
 * Created by xyoye on 2020/12/4.
 */

class PagingFooterViewHolder(
    parent: ViewGroup,
    private val retryCallback: (() -> Unit)?
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_paging_footer, parent, false)
) {

    private val binding = DataBindingUtil.bind<ItemPagingFooterBinding>(itemView)!!
    private val loadingAnimation =
        AnimationUtils.loadAnimation(itemView.context, R.anim.anim_footer_loading)

    init {
        binding.loadFailedLl.setOnClickListener {
            retryCallback?.invoke()
        }
    }

    fun setState(loadState: LoadState) {
        binding.loadingLl.isVisible = loadState is LoadState.Loading
        binding.loadFailedLl.isVisible = loadState is LoadState.Error

        if (loadState is LoadState.Error){
            val error = loadState.error
            var message = "加载失败，请点击重试"
            if(error is RequestError){
                message = "${error.msg}，请点击重试"
            }
            binding.loadFailedMsgTv.text = message
        }

        if (loadState is LoadState.Loading) {
            binding.loadingIv.animation = loadingAnimation
        } else {
            binding.loadingIv.clearAnimation()
        }
    }
}