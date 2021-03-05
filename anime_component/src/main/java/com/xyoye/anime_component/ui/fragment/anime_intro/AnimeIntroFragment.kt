package com.xyoye.anime_component.ui.fragment.anime_intro

import android.os.Bundle
import androidx.core.view.isVisible
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.FragmentAnimeIntroBinding
import com.xyoye.anime_component.databinding.ItemAnimeTagBinding
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.horizontal
import com.xyoye.common_component.extension.setData
import com.xyoye.data_component.data.BangumiData
import com.xyoye.data_component.data.TagData

class AnimeIntroFragment : BaseFragment<AnimeIntroFragmentViewModel, FragmentAnimeIntroBinding>() {

    companion object {
        fun newInstance(bangumiData: BangumiData): AnimeIntroFragment {
            val introFragment = AnimeIntroFragment()
            val bundle = Bundle()
            bundle.putParcelable("bangumi_data", bangumiData)
            introFragment.arguments = bundle
            return introFragment
        }
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            AnimeIntroFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_anime_intro

    override fun initView() {
        arguments?.run {
            getParcelable<BangumiData>("bangumi_data")?.let {
                viewModel.setBangumiData(it)
            }
        }

        dataBinding.tagRv.apply {
            layoutManager = horizontal()
            adapter = buildAdapter<TagData> {
                addItem<TagData, ItemAnimeTagBinding>(R.layout.item_anime_tag) {
                    initView { data, _, _ ->
                        itemBinding.tagTv.text = data.name
                        itemBinding.tagTv.setOnClickListener {
                            ARouter.getInstance()
                                .build(RouteTable.Anime.AnimeTag)
                                .withInt("tagId", data.id)
                                .withString("tagName", data.name)
                                .navigation()
                        }
                    }
                }
            }
        }

        viewModel.tagLiveData.observe(this) {
            if (it.size > 0) {
                dataBinding.tagRv.isVisible = true
                dataBinding.tagRv.setData(it)
            }
        }
    }
}