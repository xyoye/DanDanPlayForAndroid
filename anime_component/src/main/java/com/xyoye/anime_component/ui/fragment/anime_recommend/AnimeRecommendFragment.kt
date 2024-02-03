package com.xyoye.anime_component.ui.fragment.anime_recommend

import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.FragmentAnimeRecommendBinding
import com.xyoye.anime_component.databinding.ItemAnimeRecommendBinding
import com.xyoye.anime_component.ui.activities.anime_detail.AnimeDetailViewModel
import com.xyoye.anime_component.ui.adapter.AnimeAdapter
import com.xyoye.anime_component.utils.loadAnimeCover
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.grid
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.view.ItemDecorationDrawable
import com.xyoye.common_component.utils.view.ItemDecorationSpace
import com.xyoye.data_component.bean.AnimeArgument
import com.xyoye.data_component.data.AnimeData

class AnimeRecommendFragment :
    BaseFragment<AnimeRecommendFragmentViewModel, FragmentAnimeRecommendBinding>() {

    private val parentViewModel: AnimeDetailViewModel by viewModels(ownerProducer = { mAttachActivity })

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            AnimeRecommendFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_anime_recommend

    override fun initView() {

        initRv()

        initObserver()
    }

    private fun initRv() {
        dataBinding.recommendRv.apply {
            layoutManager = grid(3)

            val pxValue = dp2px(10)
            val spaceColor = R.color.item_bg_color.toResColor(mAttachActivity)
            addItemDecoration(ItemDecorationDrawable(pxValue, pxValue, spaceColor))

            adapter = AnimeAdapter.getAdapter(mAttachActivity)
        }

        dataBinding.recommendMoreRv.apply {
            layoutManager = grid(2)

            adapter = buildAdapter {
                addItem<AnimeData, ItemAnimeRecommendBinding>(R.layout.item_anime_recommend) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            animeCoverIv.loadAnimeCover(data.imageUrl)
                            animeTitleTv.text = data.animeTitle
                            animeStatusTv.text = if (data.isOnAir) "连载中" else "已完结"
                            itemLayout.setOnClickListener {
                                ViewCompat.setTransitionName(animeCoverIv, "cover_image")
                                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    mAttachActivity, animeCoverIv, animeCoverIv.transitionName
                                )
                                ARouter.getInstance()
                                    .build(RouteTable.Anime.AnimeDetail)
                                    .withParcelable("animeArgument", AnimeArgument.fromData(data))
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
        parentViewModel.animeDetailLiveData.observe(this) {
            viewModel.setBangumiData(it)
            dataBinding.recommendRv.setData(it.relateds)
            dataBinding.recommendMoreRv.setData(it.similars)
        }
    }
}