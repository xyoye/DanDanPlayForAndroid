package com.xyoye.anime_component.ui.activities.anime_history

import android.view.KeyEvent
import android.view.Menu
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.ActivityAnimeHistoryBinding
import com.xyoye.anime_component.databinding.ItemAnimeBinding
import com.xyoye.anime_component.ui.widget.AnimeSearchMenus
import com.xyoye.anime_component.utils.loadAnimeCover
import com.xyoye.common_component.adapter.addEmptyView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.collectAtStarted
import com.xyoye.common_component.extension.gridEmpty
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.utils.FastClickFilter
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.view.ItemDecorationDrawable
import com.xyoye.data_component.bean.AnimeArgument
import com.xyoye.data_component.data.CloudHistoryData
import com.xyoye.data_component.data.CloudHistoryListData

@Route(path = RouteTable.Anime.AnimeHistory)
class AnimeHistoryActivity : BaseActivity<AnimeHistoryViewModel, ActivityAnimeHistoryBinding>() {

    @Autowired
    @JvmField
    var historyData: CloudHistoryListData? = null

    //  标题栏搜索菜单
    private var mMenus: AnimeSearchMenus? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            AnimeHistoryViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_anime_history

    override fun initView() {
        ARouter.getInstance().inject(this)

        title = "云端播放历史"

        initRv()

        if (historyData == null) {
            viewModel.getCloudHistory()
        } else {
            dataBinding.historyRv.setData(historyData!!.playHistoryAnimes)
        }

        viewModel.displayHistoriesFlow.collectAtStarted(this) {
            dataBinding.historyRv.setData(it)
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

    private fun initRv() {
        dataBinding.historyRv.apply {

            layoutManager = gridEmpty(3)

            adapter = buildAdapter {

                addEmptyView(R.layout.layout_empty)

                addItem<CloudHistoryData, ItemAnimeBinding>(R.layout.item_anime) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            coverIv.loadAnimeCover(data.imageUrl)
                            animeNameTv.text = data.animeTitle
                            itemLayout.setOnClickListener {
                                //防止快速点击
                                if (FastClickFilter.isNeedFilter()) {
                                    return@setOnClickListener
                                }

                                ViewCompat.setTransitionName(coverIv, "cover_image")
                                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    this@AnimeHistoryActivity, coverIv, coverIv.transitionName
                                )

                                ARouter.getInstance()
                                    .build(RouteTable.Anime.AnimeDetail)
                                    .withParcelable("animeArgument", AnimeArgument.fromData(data))
                                    .withOptionsCompat(options)
                                    .navigation(this@AnimeHistoryActivity)
                            }
                        }
                    }
                }
            }

            val pxValue = dp2px(10)
            addItemDecoration(
                ItemDecorationDrawable(
                    pxValue,
                    pxValue,
                    R.color.item_bg_color.toResColor()
                )
            )
        }
    }
}