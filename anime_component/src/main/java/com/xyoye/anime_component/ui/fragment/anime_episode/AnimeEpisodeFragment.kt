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
import com.xyoye.anime_component.ui.activities.anime_detail.AnimeDetailViewModel
import com.xyoye.common_component.adapter.BaseAdapter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.setupDiffUtil
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.collectAtStarted
import com.xyoye.common_component.extension.grid
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.extension.toResString
import com.xyoye.common_component.extension.toText
import com.xyoye.common_component.utils.view.ItemDecorationSpace
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.data.EpisodeData
import java.util.regex.Pattern

class AnimeEpisodeFragment :
    BaseFragment<AnimeEpisodeFragmentViewModel, FragmentAnimeEpisodeBinding>() {

    private var episodeAsc = true
    private var inMarkMode = false

    private val episodes = mutableListOf<EpisodeData>()

    private val parentViewModel: AnimeDetailViewModel by viewModels(ownerProducer = { mAttachActivity })

    private lateinit var episodeAdapter: BaseAdapter

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            AnimeEpisodeFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_anime_episode

    override fun initView() {

        episodeAdapter = buildAdapter {
            setupDiffUtil {
                areItemsTheSame(isSameEpisodeItem())
            }

            addItem<EpisodeData, ItemAnimeEpisodeBinding>(R.layout.item_anime_episode) {
                initView { data, _, _ ->
                    itemBinding.apply {
                        // 设置选中样式
                        itemLayout.isSelected = data.selected

                        // 提取剧集内容
                        val episodeInfo = getEpisodeInfo(data.episodeTitle)
                        episodeNumberTv.text = episodeInfo[0]
                        episodeTitleTv.text = episodeInfo[1]

                        // 展示最后观看时间
                        lastWatchTv.isGone = data.lastWatched == null
                        lastWatchTv.text = formatLastPlayTime(data.lastWatched)

                        // 显示视频播放
                        ivVideoPlay.isVisible = data.histories.isNotEmpty()

                        val numberColor = if (data.lastWatched == null) R.color.text_black else R.color.text_gray
                        val titleColor = if (data.lastWatched == null) R.color.text_gray else R.color.text_gray_light
                        episodeNumberTv.setTextColorRes(numberColor)
                        episodeTitleTv.setTextColorRes(titleColor)

                        itemLayout.setOnClickListener {
                            if (inMarkMode) {
                                markItemSelected(data)
                            } else {
                                launchSearch(episodeInfo[0])
                            }
                        }

                        itemLayout.setOnLongClickListener {
                            if (inMarkMode) {
                                return@setOnLongClickListener false
                            }
                            enterMarkMode(data)
                            return@setOnLongClickListener true
                        }

                        ivVideoPlay.setOnClickListener {
                            playHistoryVideo(data)
                        }
                    }

                }
            }
        }

        dataBinding.episodeRv.apply {
            layoutManager = grid(2)

            adapter = episodeAdapter

            addItemDecoration(ItemDecorationSpace(10))
        }

        initObserver()

        initListener()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshEpisodeHistory()
    }

    private fun isSameEpisodeItem() = { old: Any, new: Any ->
        val oldItem = old as? EpisodeData
        val newItem = new as? EpisodeData
        oldItem?.episodeId == newItem?.episodeId
    }

    private fun initObserver() {
        parentViewModel.animeDetailLiveData.observe(this) {
            viewModel.setBangumiData(it)
        }

        viewModel.episodeLiveData.observe(this) {
            inMarkMode = false
            updateLayoutByMarkMode()

            val newData = if (episodeAsc.not()) {
                it.reversed()
            } else {
                it
            }

            updateEpisode(newData)
        }

        viewModel.episodeSortLiveData.observe(this) {
            episodeAsc = !episodeAsc
            updateLayoutBySort()

            val newData = episodes.reversed()
            updateEpisode(newData)
        }

        viewModel.playVideoFLow.collectAtStarted(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
        }
    }

    private fun initListener() {
        dataBinding.tvMarkAll.setOnClickListener {
            markAllItemSelected()
        }

        dataBinding.tvInvertMark.setOnClickListener {
            invertMarkItemSelected()
        }

        dataBinding.tvExitMark.setOnClickListener {
            exitMarkMode()
        }

        dataBinding.tvSetRead.setOnClickListener {
            submitSelected()
        }
    }

    /**
     * 根据排序方式改变布局
     */
    private fun updateLayoutBySort() {
        val color = if (episodeAsc) R.color.text_gray else R.color.text_blue

        val colorState = AppCompatResources.getColorStateList(mAttachActivity, color)
        ImageViewCompat.setImageTintList(dataBinding.sortIv, colorState)

        dataBinding.sortTv.text = if (episodeAsc) "正序" else "倒序"
        dataBinding.sortTv.setTextColorRes(color)
    }

    /**
     * 提取剧集名称及话数
     */
    private fun getEpisodeInfo(episodeTitle: String?): Array<String> {
        val episodeInfo = episodeTitle?.split("\\s".toRegex())
        if (episodeInfo != null) {
            return when {
                episodeInfo.size > 1 -> {
                    val title = episodeTitle.substring(episodeInfo[0].length + 1)
                    arrayOf(episodeInfo[0], title)
                }

                episodeInfo.size == 1 -> {
                    arrayOf(episodeInfo[0], "未知剧集")
                }

                else -> {
                    arrayOf("第1话", "未知剧集")
                }
            }
        } else {
            return arrayOf("第1话", "未知剧集")
        }
    }

    /**
     * 格式化最后播放时间
     */
    private fun formatLastPlayTime(time: String?): String {
        if (time.isNullOrEmpty()) {
            return ""
        }
        return try {
            viewModel.utcTimeFormat.parse(time).toText()
        } catch (e: Exception) {
            e.printStackTrace()
            time
        }
    }

    /**
     * 跳转至搜索页
     */
    private fun launchSearch(episodeNumber: String) {
        val episodeNum = getEpisodeNum(episodeNumber)
        ARouter.getInstance()
            .build(RouteTable.Anime.Search)
            .withString(
                "animeTitle",
                viewModel.animeTitleField.get()
            )
            .withString(
                "searchWord",
                "${viewModel.animeSearchWordField.get()} $episodeNum"
            )
            .withBoolean("isSearchMagnet", true)
            .navigation()
    }

    /**
     * 提取剧集中的集数，用于搜索
     *
     * 例：第5话  ->  5
     */
    private fun getEpisodeNum(episodeText: String?): String {
        if (episodeText == null)
            return ""
        val pattern = Pattern.compile("第(\\d+)话")
        val matcher = pattern.matcher(episodeText)
        if (matcher.find()) {
            return matcher.group().run {
                var episode = substring(1, length - 1)
                if (episode.length == 1) {
                    episode = "0$episode"
                }
                episode
            }
        }
        return ""
    }

    /**
     * 进入标记模式
     */
    private fun enterMarkMode(data: EpisodeData) {
        inMarkMode = true
        updateLayoutByMarkMode()

        // 如果当前长按剧集未看，默认选中
        if (data.lastWatched == null) {
            markItemSelected(data, reset = true)
        }
    }

    /**
     * 退出标记模式
     */
    private fun exitMarkMode() {
        inMarkMode = false
        updateLayoutByMarkMode()

        val newData = episodes.map {
            it.copy(selected = false)
        }
        updateEpisode(newData)
    }

    /**
     * 根据标记模式改变布局
     */
    private fun updateLayoutByMarkMode() {
        dataBinding.actionLayout.isVisible = inMarkMode
        dataBinding.titleCl.isVisible = inMarkMode.not()
    }

    /**
     * 标记Item为已选中
     */
    private fun markItemSelected(data: EpisodeData, reset: Boolean = false) {
        val newData = episodes.map {
            if (reset) {
                it.copy(selected = it.lastWatched == null && it.episodeId == data.episodeId)
            } else {
                if (it.lastWatched == null && it.episodeId == data.episodeId) {
                    it.copy(selected = it.selected.not())
                } else {
                    it
                }
            }
        }
        updateEpisode(newData)
    }

    /**
     * 标记所有Item为已选中
     */
    private fun markAllItemSelected() {
        val newData = episodes.map {
            if (it.lastWatched == null) {
                it.copy(selected = true)
            } else {
                it
            }
        }
        updateEpisode(newData)
    }

    /**
     * 反选Item选中状态
     */
    private fun invertMarkItemSelected() {
        val newData = episodes.map {
            if (it.lastWatched == null) {
                it.copy(selected = it.selected.not())
            } else {
                it
            }
        }
        updateEpisode(newData)
    }

    /**
     * 提交已选中数据
     */
    private fun submitSelected() {
        val selectedEpisodeIds = episodes.filter { it.selected }.map { it.episodeId }
        if (selectedEpisodeIds.isEmpty()) {
            ToastCenter.showError("最少标记1集为已看")
            return
        }
        if (selectedEpisodeIds.size > 100) {
            ToastCenter.showError("单次最多标记100集为已看，当前选中: ${selectedEpisodeIds.size}")
            return
        }

        viewModel.submitEpisodeRead(selectedEpisodeIds)
    }

    /**
     * 更新剧集列表
     */
    private fun updateEpisode(episodeList: List<EpisodeData>) {
        episodes.clear()
        episodes.addAll(episodeList)
        dataBinding.episodeRv.setData(episodeList)

        if (inMarkMode) {
            val selectedCount = episodes.count { it.selected }
            val display =
                if (selectedCount > 0) "此${selectedCount}集已看" else R.string.action_mark_as_viewed.toResString()
            dataBinding.tvSetRead.text = display
        } else {
            dataBinding.tvSetRead.text = R.string.action_mark_as_viewed.toResString()
        }
    }

    /**
     * 播放媒体库中的视频
     */
    private fun playHistoryVideo(episode: EpisodeData) {
        val actionList = episode.histories.mapIndexedNotNull { index, history ->
            val library = history.library ?: return@mapIndexedNotNull null
            SheetActionBean(index, library.displayName, library.mediaType.cover, library.disPlayDescribe)
        }
        BottomActionDialog(mAttachActivity, actionList, "选择媒体库") {
            viewModel.playEpisodeHistory(
                episode.histories[it.actionId as Int].entity
            )
            return@BottomActionDialog true
        }.show()
    }
}