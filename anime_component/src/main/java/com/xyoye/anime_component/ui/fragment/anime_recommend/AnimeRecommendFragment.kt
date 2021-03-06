package com.xyoye.anime_component.ui.fragment.anime_recommend

import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.FragmentAnimeRecommendBinding
import com.xyoye.anime_component.databinding.ItemAnimeRecommendBinding
import com.xyoye.anime_component.ui.adapter.AnimeAdapter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.grid
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.setGlideImage
import com.xyoye.common_component.utils.view.ItemDecorationDrawable
import com.xyoye.common_component.utils.view.ItemDecorationSpace
import com.xyoye.common_component.utils.dp2px
import com.xyoye.data_component.data.AnimeData
import com.xyoye.data_component.data.BangumiData

class AnimeRecommendFragment :
    BaseFragment<AnimeRecommendFragmentViewModel, FragmentAnimeRecommendBinding>() {

    companion object {
        fun newInstance(bangumiData: BangumiData): AnimeRecommendFragment {
            val recommendFragment = AnimeRecommendFragment()
            val bundle = Bundle()
            bundle.putParcelable("bangumi_data", bangumiData)
            recommendFragment.arguments = bundle
            return recommendFragment
        }
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            AnimeRecommendFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_anime_recommend

    override fun initView() {

        initRv()

        initObserver()

        arguments?.run {
            getParcelable<BangumiData>("bangumi_data")?.let {
                viewModel.setBangumiData(it)
            }
        }
    }

    private fun initRv() {

        dataBinding.recommendRv.apply {
            layoutManager = grid(3)

            val pxValue = dp2px(10)
            val spaceColor = ContextCompat.getColor(mAttachActivity, R.color.item_bg_color)
            addItemDecoration(ItemDecorationDrawable(pxValue, pxValue, spaceColor))

            adapter = AnimeAdapter.getAdapter(mAttachActivity)
        }

        dataBinding.recommendMoreRv.apply {
            layoutManager = grid(2)

            adapter = buildAdapter<AnimeData> {
                addItem<AnimeData, ItemAnimeRecommendBinding>(R.layout.item_anime_recommend) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            animeCoverIv.setGlideImage(data.imageUrl, 3)
                            animeTitleTv.text = data.animeTitle
                            animeStatusTv.text = if (data.isOnAir) "连载中" else "已完结"
                            itemLayout.setOnClickListener {
                                ViewCompat.setTransitionName(animeCoverIv, "cover_image")
                                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    mAttachActivity, animeCoverIv, animeCoverIv.transitionName
                                )
                                ARouter.getInstance()
                                    .build(RouteTable.Anime.AnimeDetail)
                                    .withInt("animeId", data.animeId)
                                    .withOptionsCompat(options)
                                    .navigation(mAttachActivity)
                            }
                        }
                    }
                }
            }

            addItemDecoration(ItemDecorationSpace(10))
        }
    }

    private fun initObserver() {
        viewModel.recommendLiveData.observe(this) {
            dataBinding.recommendRv.setData(it)
        }

        viewModel.recommendMoreLiveData.observe(this) {
            dataBinding.recommendMoreRv.setData(it)
        }
    }
}