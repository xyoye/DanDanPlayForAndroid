package com.xyoye.anime_component.ui.activities.anime_detail

import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.android.material.tabs.TabLayoutMediator
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.ActivityAnimeDetailBinding
import com.xyoye.anime_component.ui.adapter.AnimeDetailPageAdapter
import com.xyoye.anime_component.utils.loadAnimeCover
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.clamp
import com.xyoye.common_component.extension.dp
import com.xyoye.common_component.extension.isNightMode
import com.xyoye.common_component.extension.loadImage
import com.xyoye.common_component.extension.opacity
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.extension.toResColor
import com.xyoye.data_component.bean.AnimeArgument
import com.xyoye.data_component.bean.ColorRange
import com.xyoye.data_component.enums.AnimeDetailTab
import kotlin.math.absoluteValue

@Route(path = RouteTable.Anime.AnimeDetail)
class AnimeDetailActivity : BaseActivity<AnimeDetailViewModel, ActivityAnimeDetailBinding>() {

    @Autowired
    @JvmField
    var animeArgument: AnimeArgument = AnimeArgument()

    // 标题栏字体颜色变化范围
    private val textColorRange by lazy {
        ColorRange(
            R.color.text_white_immutable.toResColor(this),
            R.color.text_theme.toResColor(this)
        )
    }

    // 页面颜色变化范围
    private val pageColorRange by lazy {
        ColorRange(
            R.color.item_bg_color.toResColor(this),
            R.color.theme.toResColor(this)
        )
    }

    // 上一次滚动的百分比
    private var lastScrollLimit = 0

    // 默认的tab列表
    private val defaultTabs = AnimeDetailTab.values()

    override fun initViewModel() = ViewModelInit(
        BR.viewModel,
        AnimeDetailViewModel::class.java
    )

    override fun getLayoutId() = R.layout.activity_anime_detail

    override fun initStatusBar() {
        ImmersionBar.with(this)
            .transparentBar()
            .statusBarDarkFont(false)
            .init()
    }

    override fun initView() {
        ARouter.getInstance().inject(this)

        title = ""

        initViewPager(defaultTabs)
        initListener()

        updateUIByScroll(0f)
        updateTabLayoutRound(0f)
        updateUIbyAnime(animeArgument.title, animeArgument.imageUrl)

        viewModel.animeIdField.set(animeArgument.id.toString())
        viewModel.animeTitleField.set(animeArgument.title)
        viewModel.getAnimeDetail(animeArgument.id.toString())
    }

    private fun initViewPager(tabs: Array<AnimeDetailTab>) {
        dataBinding.viewpager.adapter = AnimeDetailPageAdapter(this, tabs)
        val mediator = TabLayoutMediator(
            dataBinding.tabLayout,
            dataBinding.viewpager
        ) { tab, position ->
            tab.text = tabs[position].title
        }
        mediator.attach()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        // 重写以防止返回动画
        finish()
    }

    private fun initListener() {
        dataBinding.appBarLayout.addOnOffsetChangedListener { layout, offset ->
            val percent = offset.absoluteValue.toFloat() / layout.totalScrollRange * 2f
            // 降低滚动更新UI的频率
            val scrollLimit = (percent * 100).toInt()
            if (scrollLimit != lastScrollLimit) {
                lastScrollLimit = scrollLimit
                // 前半段滚动，更新颜色
                updateUIByScroll(percent.clamp(0f, 1f))
                // 后半段滚动，更新圆角
                val roundedPercent = (scrollLimit - 100) / 100f
                updateTabLayoutRound(roundedPercent.clamp(0f, 1f))
            }
        }

        viewModel.followLiveData.observe(this) { followed ->
            if (followed) {
                dataBinding.followTv.text = "已追番"
                dataBinding.followTv.isSelected = true
                dataBinding.followTv.setTextColorRes(R.color.text_theme)
            } else {
                dataBinding.followTv.text = "追番"
                dataBinding.followTv.isSelected = false
                dataBinding.followTv.setTextColorRes(R.color.text_orange)
            }
        }

        viewModel.animeDetailLiveData.observe(this) {
            if (it.animeTitle != animeArgument.title || it.imageUrl != animeArgument.imageUrl) {
                updateUIbyAnime(it.animeTitle.orEmpty(), it.imageUrl.orEmpty())
            }

            // 如果没有相关推荐和相似番剧，移除推荐tab
            val dynamicTabs = defaultTabs.toMutableList()
            if (it.relateds.isEmpty() && it.similars.isEmpty()) {
                dynamicTabs.remove(AnimeDetailTab.RECOMMEND)
                initViewPager(dynamicTabs.toTypedArray())
            }
        }
    }

    /**
     * 根据滚动百分比更新UI
     */
    private fun updateUIByScroll(percent: Float) {
        // 标题栏颜色
        dataBinding.toolbar.navigationIcon?.setTint(textColorRange.take(percent))
        dataBinding.toolbar.setTitleTextColor(pageColorRange.end.opacity(percent))
        dataBinding.toolbar.setBackgroundColor(pageColorRange.start.opacity(percent))
        // 追番按钮背景
        dataBinding.followTv.background?.alpha = 255 - (255 * percent).toInt()

        //状态栏文字颜色
        //tips: MIUI深色模式下状态栏字体颜色不 受此控制
        val isDarkFont = percent > 0.5f && !isNightMode()
        ImmersionBar.with(this)
            .statusBarColorInt(pageColorRange.start.opacity(percent))
            .statusBarDarkFont(isDarkFont)
            .init()
    }

    /**
     * 根据滚动百分比更新TabLayout圆角
     */
    private fun updateTabLayoutRound(percent: Float) {
        val topRadius = 12.dp() * (1 - percent)
        val bottomRadius = 0f
        val roundCorners = floatArrayOf(
            topRadius, topRadius,
            topRadius, topRadius,
            bottomRadius, bottomRadius,
            bottomRadius, bottomRadius
        )
        dataBinding.tabLayout.background = ShapeDrawable().apply {
            shape = RoundRectShape(roundCorners, null, null)
            paint.color = pageColorRange.start
        }
    }

    /**
     * 根据番剧信息更新UI
     */
    private fun updateUIbyAnime(title: String, image: String) {
        dataBinding.collapsingToolbarLayout.title = title
        dataBinding.backgroundCoverIv.loadImage(image)
        dataBinding.coverIv.loadAnimeCover(image)
    }
}