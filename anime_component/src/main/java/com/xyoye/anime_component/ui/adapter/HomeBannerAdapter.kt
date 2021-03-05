package com.xyoye.anime_component.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.ItemBannerBinding
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.setGlideImage
import com.xyoye.data_component.data.BannerDetailData
import com.youth.banner.adapter.BannerAdapter

/**
 * Created by xyoye on 2020/7/31.
 */

class HomeBannerAdapter(bannerDetails: MutableList<BannerDetailData>) :
    BannerAdapter<BannerDetailData, HomeBannerAdapter.BannerHolder>(bannerDetails) {

    inner class BannerHolder(@NonNull binding: ItemBannerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): BannerHolder {
        return BannerHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent!!.context),
                R.layout.item_banner,
                parent,
                false
            )
        )
    }

    override fun onBindView(
        holder: BannerHolder?,
        data: BannerDetailData?,
        position: Int,
        size: Int
    ) {
        if (holder != null && data != null) {
            DataBindingUtil.getBinding<ItemBannerBinding>(holder.itemView)?.apply {
                bannerIv.setGlideImage(data.imageUrl)
                bannerTitleTv.text = data.title
                itemLayout.setOnClickListener {
                    ARouter.getInstance().build(RouteTable.User.WebView)
                        .withString("titleText", data.title)
                        .withString("url", data.url)
                        .navigation()
                }
            }
        }
    }
}