package com.xyoye.anime_component.ui.fragment.anime_episode

import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.viewModels
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.FragmentAnimeEpisodeBinding
import com.xyoye.anime_component.databinding.ItemAnimeEpisodeBinding
import com.xyoye.anime_component.ui.activities.anime_detail.AnimeDetailActivity
import com.xyoye.anime_component.ui.activities.anime_detail.AnimeDetailViewModel
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.setupDiffUtil
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.config.UserConfig
import com.xyoye.common_component.extension.collectAtStarted
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.extension.toResString
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.data.EpisodeData

class AnimeEpisodeFragment :
    BaseFragment<AnimeEpisodeFragmentViewModel, FragmentAnimeEpisodeBinding>() {

    private val parentViewModel: AnimeDetailViewModel by viewModels(ownerProducer = { mAttachActivity })

    // 番剧详情Activity
    private val animeDetailActivity get() = mAttachActivity as? AnimeDetailActivity

    // 返回事件拦截器
    private val backPressInterceptor: () -> Boolean = Interceptor@{
        if (viewModel.markModeFlow.value) {
            viewModel.toggleMarkMode(false)
            return@Interceptor true
        }
        return@Interceptor false
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            AnimeEpisodeFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_anime_episode

    override fun initView() {

        val episodeAdapter = buildAdapter {
            setupDiffUtil {
                areItemsTheSame { old: Any, new: Any ->
                    val oldItem = old as? EpisodeData
                    val newItem = new as? EpisodeData
                    oldItem?.episodeId == newItem?.episodeId
                }
            }

            addItem<EpisodeData, ItemAnimeEpisodeBinding>(R.layout.item_anime_episode) {
                initView { data, _, _ ->
                    itemBinding.apply {
                        // 剧集信息
                        tvEpisodeTitle.text = data.title
                        tvEpisodeSubtitle.text = data.subtitle

                        // 观看时间
                        groupWatchTime.isGone = data.watchTime.isNullOrEmpty()
                        tvWatchTime.text = data.watchTime

                        // 已看状态
                        tvEpisodeTitle.isSelected = data.watched
                        cbEpisodeMark.isChecked = data.isMarked

                        // 按钮的显示
                        ivEpisodePlay.isVisible = data.inMarkMode.not() && data.histories.isNotEmpty()
                        ivEpisodeMarkViewed.isVisible = data.inMarkMode.not() && data.markAble
                        flEpisodeMark.isVisible = data.inMarkMode

                        ivEpisodeMarkViewed.setOnClickListener {
                            considerMarkAsViewed(data)
                        }

                        itemLayout.setOnClickListener {
                            if (viewModel.markModeFlow.value) {
                                viewModel.markEpisodeById(data.episodeId)
                            } else {
                                searchEpisodeResource(data)
                            }
                        }

                        itemLayout.setOnLongClickListener {
                            viewModel.toggleMarkMode(true)
                            return@setOnLongClickListener true
                        }

                        ivEpisodePlay.setOnClickListener {
                            playStorageEpisode(data)
                        }
                    }

                }
            }
        }

        dataBinding.episodeRv.apply {
            layoutManager = vertical()
            adapter = episodeAdapter
        }

        initObserver()

        initListener()
    }

    override fun onResume() {
        super.onResume()
        animeDetailActivity?.registerBackPressInterceptor(backPressInterceptor)
    }

    override fun onPause() {
        super.onPause()
        animeDetailActivity?.unregisterBackPressInterceptor()
    }

    private fun initObserver() {
        parentViewModel.animeDetailLiveData.observe(this) {
            viewModel.setBangumiData(it)
        }

        viewModel.displayEpisodesFlow.collectAtStarted(this) {
            dataBinding.episodeRv.setData(it)
        }

        viewModel.ascendingFlow.collectAtStarted(this) {
            updateLayoutBySort(it)
        }

        viewModel.markModeFlow.collectAtStarted(this) {
            dataBinding.actionLayout.isVisible = it
            dataBinding.titleCl.isVisible = it.not()
        }

        viewModel.refreshBangumiFlow.collectAtStarted(this) {
            animeDetailActivity?.refreshAnimeDetail()
        }

        viewModel.playVideoFLow.collectAtStarted(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
        }
    }

    private fun initListener() {
        dataBinding.tvMarkAll.setOnClickListener {
            viewModel.markAllEpisode()
        }

        dataBinding.tvInvertMark.setOnClickListener {
            viewModel.reverseEpisodeMark()
        }

        dataBinding.tvExitMark.setOnClickListener {
            viewModel.toggleMarkMode(false)
        }

        dataBinding.tvSetRead.setOnClickListener {
            if (UserConfig.isUserLoggedIn().not()) {
                ToastCenter.showWarning(R.string.tips_login_required.toResString())
                return@setOnClickListener
            }
            viewModel.submitMarkedEpisodesViewed()
        }
    }

    /**
     * 根据排序方式改变布局
     */
    private fun updateLayoutBySort(ascending: Boolean) {
        val color = if (ascending) R.color.text_gray else R.color.text_blue
        val colorState = AppCompatResources.getColorStateList(mAttachActivity, color)
        ImageViewCompat.setImageTintList(dataBinding.sortIv, colorState)

        dataBinding.sortTv.text = if (ascending) "正序" else "倒序"
        dataBinding.sortTv.setTextColorRes(color)
    }

    /**
     * 搜索剧集资源
     */
    private fun searchEpisodeResource(data: EpisodeData) {
        ARouter.getInstance()
            .build(RouteTable.Anime.Search)
            .withString(
                "animeTitle",
                viewModel.animeTitleField.get()
            )
            .withString(
                "searchWord",
                "${viewModel.animeSearchWordField.get()} ${data.searchEpisodeNum}"
            )
            .withBoolean("isSearchMagnet", true)
            .navigation()
    }

    /**
     * 考虑标记为已看
     */
    private fun considerMarkAsViewed(data: EpisodeData) {
        if (UserConfig.isUserLoggedIn().not()) {
            ToastCenter.showWarning(R.string.tips_login_required.toResString())
            return
        }

        CommonDialog.Builder(mAttachActivity).apply {
            content = "确认标记 ${data.title} 为已看？"
            addPositive {
                viewModel.submitEpisodesViewed(listOf(data.episodeId))
                it.dismiss()
            }
            addNegative {
                it.dismiss()
            }
        }.build().show()
    }

    /**
     * 播放媒体库中的视频
     */
    private fun playStorageEpisode(episode: EpisodeData) {
        val actionList = episode.histories.mapIndexedNotNull { index, history ->
            val library = history.library ?: return@mapIndexedNotNull null
            SheetActionBean(index, library.displayName, library.mediaType.cover, history.entity.storagePath)
        }
        BottomActionDialog(mAttachActivity, actionList, "选择播放记录") {
            viewModel.playLocalEpisode(
                episode.histories[it.actionId as Int]
            )
            return@BottomActionDialog true
        }.show()
    }
}