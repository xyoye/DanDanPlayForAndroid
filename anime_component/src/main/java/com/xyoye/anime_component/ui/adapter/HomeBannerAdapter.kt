package com.xyoye.anime_component.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.therouter.TheRouter
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.ItemBannerBinding
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.loadImage
import com.xyoye.data_component.data.BannerDetailData
import com.youth.banner.adapter.BannerAdapter

/**
 * Created by xyoye on 2020/7/31.
 */

class HomeBannerAdapter(bannerDetails: List<BannerDetailData>) :
    BannerAdapter<BannerDetailData, HomeBannerAdapter.BannerHolder>(bannerDetails) {

    inner class BannerHolder(binding: ItemBannerBinding) :
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
                bannerIv.loadImage(data.imageUrl)
                bannerTitleTv.text = data.title
                itemLayout.setOnClickListener {
                    TheRouter.build(RouteTable.User.WebView)
                        .withString("titleText", data.title)
                        .withString("url", data.url)
                        .navigation()
                }
            }
        }
    }
}