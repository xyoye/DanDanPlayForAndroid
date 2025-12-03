package com.xyoye.anime_component.ui.activities.anime_follow

import android.view.KeyEvent
import android.view.Menu
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.ActivityAnimeFollowBinding
import com.xyoye.anime_component.ui.adapter.AnimeAdapter
import com.xyoye.anime_component.ui.widget.AnimeSearchMenus
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.collectAtStarted
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

    // 标题栏搜索菜单
    private var mMenus: AnimeSearchMenus? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            AnimeFollowViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_anime_follow

    override fun initView() {
        TheRouter.inject(this)

        title = "我的追番"

        dataBinding.followRv.apply {

            layoutManager = gridEmpty(3)

            adapter = animeAdapter

            val pxValue = dp2px(10)
            addItemDecoration(
                ItemDecorationDrawable(
                    pxValue,
                    pxValue,
                    com.xyoye.common_component.R.color.item_bg_color.toResColor(this@AnimeFollowActivity)
                )
            )
        }

        if (followData == null) {
            viewModel.getUserFollow()
        } else {
            dataBinding.followRv.setData(followData!!.favorites)
        }

        viewModel.displayFollowedFlow.collectAtStarted(this) {
            dataBinding.followRv.setData(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mMenus = AnimeSearchMenus.inflater(this, menu).apply {
            onSearchTextChanged { viewModel.searchAnime(it) }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && mMenus?.handleBackPressed() == true) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}