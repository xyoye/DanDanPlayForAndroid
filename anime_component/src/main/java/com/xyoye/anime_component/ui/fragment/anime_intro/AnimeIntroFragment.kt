package com.xyoye.anime_component.ui.fragment.anime_intro

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.therouter.TheRouter
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.FragmentAnimeIntroBinding
import com.xyoye.anime_component.databinding.ItemAnimeTagBinding
import com.xyoye.anime_component.ui.activities.anime_detail.AnimeDetailViewModel
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.horizontal
import com.xyoye.common_component.extension.setData
import com.xyoye.data_component.data.TagData

class AnimeIntroFragment : BaseFragment<AnimeIntroFragmentViewModel, FragmentAnimeIntroBinding>() {

    private val parentViewModel: AnimeDetailViewModel by viewModels(ownerProducer = { mAttachActivity })

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            AnimeIntroFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_anime_intro

    override fun initView() {
        dataBinding.tagRv.apply {
            layoutManager = horizontal()
            adapter = buildAdapter {
                addItem<TagData, ItemAnimeTagBinding>(R.layout.item_anime_tag) {
                    initView { data, _, _ ->
                        itemBinding.tagTv.text = data.name
                        itemBinding.tagTv.setOnClickListener {
                            TheRouter
                                .build(RouteTable.Anime.AnimeTag)
                                .withInt("tagId", data.id)
                                .withString("tagName", data.name)
                                .navigation()
                        }
                    }
                }
            }
        }

        parentViewModel.animeDetailLiveData.observe(this) {
            viewModel.setBangumiData(it)

            dataBinding.tagRv.isVisible = it.tags.isNotEmpty()
            dataBinding.tagRv.setData(it.tags)
        }
    }
}