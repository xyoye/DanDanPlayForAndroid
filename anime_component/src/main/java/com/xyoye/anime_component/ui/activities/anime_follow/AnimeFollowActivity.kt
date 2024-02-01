package com.xyoye.anime_component.ui.activities.anime_follow

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.ActivityAnimeFollowBinding
import com.xyoye.anime_component.ui.adapter.AnimeAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.gridEmpty
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.view.ItemDecorationDrawable
import com.xyoye.data_component.data.FollowAnimeData

@Route(path = RouteTable.Anime.AnimeFollow)
class AnimeFollowActivity : BaseActivity<AnimeFollowViewModel, ActivityAnimeFollowBinding>() {
    private val animeAdapter = AnimeAdapter.getAdapter(this)

    @Autowired
    @JvmField
    var followData: FollowAnimeData? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            AnimeFollowViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_anime_follow

    override fun initView() {
        ARouter.getInstance().inject(this)

        title = "我的追番"

        dataBinding.followRv.apply {

            layoutManager = gridEmpty(3)

            adapter = animeAdapter

            val pxValue = dp2px(10)
            addItemDecoration(
                ItemDecorationDrawable(
                    pxValue,
                    pxValue,
                    R.color.item_bg_color.toResColor(this@AnimeFollowActivity)
                )
            )
        }

        if (followData == null) {
            viewModel.getUserFollow()
        } else {
            dataBinding.followRv.setData(followData!!.favorites)
        }

        viewModel.followLiveData.observe(this) {
            dataBinding.followRv.setData(it.favorites)
        }
    }
}