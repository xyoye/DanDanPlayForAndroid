package com.xyoye.anime_component.ui.fragment.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.FragmentHomeBinding
import com.xyoye.anime_component.ui.adapter.HomeBannerAdapter
import com.xyoye.anime_component.ui.fragment.home_page.HomePageFragment
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.utils.dp2px
import com.youth.banner.config.BannerConfig
import com.youth.banner.config.IndicatorConfig
import com.youth.banner.indicator.CircleIndicator
import java.util.*

/**
 * Created by xyoye on 2020/7/28.
 */

@Route(path = RouteTable.Anime.HomeFragment)
class HomeFragment : BaseFragment<HomeFragmentViewModel, FragmentHomeBinding>() {

    override fun initViewModel() = ViewModelInit(
        BR.viewModel,
        HomeFragmentViewModel::class.java
    )

    override fun getLayoutId() = R.layout.fragment_home

    override fun initView() {
        dataBinding.tabLayout.setupWithViewPager(dataBinding.viewpager)

        dataBinding.searchLl.setOnClickListener {
            ARouter.getInstance()
                .build(RouteTable.Anime.Search)
                .navigation()
        }
        dataBinding.seasonLl.setOnClickListener {
            ARouter.getInstance()
                .build(RouteTable.Anime.AnimeSeason)
                .navigation()
        }

        initViewModelObserve()
        viewModel.getBanners()
        viewModel.getWeeklyAnime()
    }

    private fun initViewModelObserve() {
        viewModel.bannersLiveData.observe(this) {
            dataBinding.banner.apply {
                setAdapter(HomeBannerAdapter(it.banners))
                indicator = CircleIndicator(mAttachActivity)
                setIndicatorGravity(IndicatorConfig.Direction.RIGHT)
                setIndicatorMargins(
                    IndicatorConfig.Margins(
                        0, 0, BannerConfig.INDICATOR_MARGIN, dp2px(12)
                    )
                )
            }
        }

        viewModel.weeklyAnimeLiveData.observe(this) {
            dataBinding.viewpager.apply {
                adapter = HomePageAdapter(childFragmentManager)
                offscreenPageLimit = 2
                currentItem = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
            }
        }
    }

    inner class HomePageAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
        fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        override fun getItem(position: Int): Fragment {
            return HomePageFragment.newInstance(
                viewModel.weeklyAnimeLiveData.value!![position]
            )
        }

        override fun getCount(): Int {
            return viewModel.weeklyAnimeLiveData.value?.size ?: 0
        }

        override fun getPageTitle(position: Int): CharSequence {
            return if (position < viewModel.tabTitles.size)
                viewModel.tabTitles[position]
            else
                ""
        }
    }
}