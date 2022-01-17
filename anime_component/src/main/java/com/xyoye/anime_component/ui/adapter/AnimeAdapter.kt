package com.xyoye.anime_component.ui.adapter

import android.app.Activity
import android.graphics.Bitmap
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.ItemAnimeBinding
import com.xyoye.anime_component.utils.AnimeDiffCallBack
import com.xyoye.anime_component.utils.PaletteBitmapTarget
import com.xyoye.common_component.adapter.BaseAdapter
import com.xyoye.common_component.adapter.addEmptyView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.config.UserConfig
import com.xyoye.common_component.utils.FastClickFilter
import com.xyoye.data_component.data.AnimeData

/**
 * Created by xyoye on 2020/10/4.
 */

class AnimeAdapter : BaseAdapter() {

    companion object {
        fun getAdapter(activity: Activity) = buildAdapter {

            addEmptyView(R.layout.layout_empty)

            addItem<AnimeData, ItemAnimeBinding>(R.layout.item_anime) {
                initView { data, _, _ ->
                    itemBinding.apply {
                        Glide.with(coverIv)
                            .asBitmap()
                            .load(data.imageUrl ?: "")
                            .transition((BitmapTransitionOptions.withCrossFade()))
                            .into(object : PaletteBitmapTarget() {
                                override fun onBitmapReady(bitmap: Bitmap, paletteColor: Int) {
                                    coverIv.setImageBitmap(bitmap)
                                    animeNameTv.setBackgroundColor(paletteColor)
                                }
                            })

                        followTagView.isGone = !UserConfig.isUserLoggedIn() || !data.isFavorited
                        animeNameTv.text = data.animeTitle
                        itemLayout.setOnClickListener {
                            //防止快速点击
                            if(FastClickFilter.isNeedFilter()){
                                return@setOnClickListener
                            }

                            ViewCompat.setTransitionName(coverIv, "cover_image")
                            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                activity, coverIv, coverIv.transitionName
                            )

                            ARouter.getInstance()
                                .build(RouteTable.Anime.AnimeDetail)
                                .withInt("animeId", data.animeId)
                                .withOptionsCompat(options)
                                .navigation(activity)
                        }
                    }
                }
            }
        }
    }
}

fun BaseAdapter.setNewAnimeData(newData: MutableList<AnimeData>) {
    val calculateResult = DiffUtil.calculateDiff(
        AnimeDiffCallBack(this.items, newData)
    )
    this.items.clear()
    this.items.addAll(newData)
    calculateResult.dispatchUpdatesTo(this)
}
