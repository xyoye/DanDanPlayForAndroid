package com.xyoye.anime_component.ui.fragment.home_page

import android.os.Bundle
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.FragmentHomePageBinding
import com.xyoye.anime_component.ui.adapter.AnimeAdapter
import com.xyoye.common_component.base.BaseAppFragment
import com.xyoye.common_component.extension.gridEmpty
import com.xyoye.common_component.extension.isLandScreen
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.view.ItemDecorationDrawable
import com.xyoye.data_component.data.BangumiAnimeData

/**
 * Created by xyoye on 2020/7/30.
 */

class HomePageFragment : BaseAppFragment<FragmentHomePageBinding>() {
    companion object {
        fun newInstance(weeklyAnimeData: BangumiAnimeData): HomePageFragment {
            val homePageFragment = HomePageFragment()
            val bundle = Bundle()
            bundle.putParcelable("anime_data", weeklyAnimeData)
            homePageFragment.arguments = bundle
            return homePageFragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_home_page

    override fun initView() {
        val spanCount = if (mAttachActivity.isLandScreen()) 4 else 3

        dataBinding.pageAnimeRv.apply {
            layoutManager = gridEmpty(spanCount)

            adapter = AnimeAdapter.getAdapter(mAttachActivity)

            val pxValue = dp2px(10)
            val spaceColor = com.xyoye.common_component.R.color.item_bg_color.toResColor(mAttachActivity)
            addItemDecoration(ItemDecorationDrawable(pxValue, pxValue, spaceColor))
        }

        arguments?.run {
            getParcelable<BangumiAnimeData>("anime_data")?.let {
                dataBinding.pageAnimeRv.setData(it.bangumiList)
            }
        }
    }
}