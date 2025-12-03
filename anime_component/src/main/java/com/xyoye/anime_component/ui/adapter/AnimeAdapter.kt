package com.xyoye.anime_component.ui.adapter

import android.app.Activity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isGone
import com.therouter.TheRouter
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.ItemAnimeBinding
import com.xyoye.anime_component.utils.loadAnimeCover
import com.xyoye.common_component.adapter.addEmptyView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.config.UserConfig
import com.xyoye.common_component.utils.FastClickFilter
import com.xyoye.data_component.bean.AnimeArgument
import com.xyoye.data_component.data.AnimeData

/**
 * Created by xyoye on 2020/10/4.
 */

class AnimeAdapter {

    companion object {
        fun getAdapter(activity: Activity) = buildAdapter {

            addEmptyView(com.xyoye.common_component.R.layout.layout_empty)

            addItem<AnimeData, ItemAnimeBinding>(R.layout.item_anime) {
                initView { data, _, _ ->
                    itemBinding.apply {
                        coverIv.loadAnimeCover(data.imageUrl)

                        followTagView.isGone = !UserConfig.getUserLoggedIn() || !data.isFavorited
                        animeNameTv.text = data.animeTitle
                        itemLayout.setOnClickListener {
                            //防止快速点击
                            if (FastClickFilter.isNeedFilter()) {
                                return@setOnClickListener
                            }

                            ViewCompat.setTransitionName(coverIv, "cover_image")
                            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                activity, coverIv, coverIv.transitionName
                            )

                            TheRouter
                                .build(RouteTable.Anime.AnimeDetail)
                                .withParcelable("animeArgument", AnimeArgument.fromData(data))
                                .withOptionsCompat(options.toBundle())
                                .navigation(activity)
                        }
                    }
                }
            }
        }
    }
}
