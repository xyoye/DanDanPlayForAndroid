package com.xyoye.anime_component.ui.fragment.anime_episode

import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isGone
import androidx.core.widget.ImageViewCompat
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.FragmentAnimeEpisodeBinding
import com.xyoye.anime_component.databinding.ItemAnimeEpisodeBinding
import com.xyoye.common_component.adapter.BaseAdapter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.grid
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.utils.view.ItemDecorationSpace
import com.xyoye.data_component.data.BangumiData
import com.xyoye.data_component.data.EpisodeData
import java.util.regex.Pattern

class AnimeEpisodeFragment :
    BaseFragment<AnimeEpisodeFragmentViewModel, FragmentAnimeEpisodeBinding>() {

    companion object {
        fun newInstance(bangumiData: BangumiData): AnimeEpisodeFragment {
            val episodeFragment = AnimeEpisodeFragment()
            val bundle = Bundle()
            bundle.putParcelable("bangumi_data", bangumiData)
            episodeFragment.arguments = bundle
            return episodeFragment
        }
    }

    private lateinit var episodeAdapter: BaseAdapter<EpisodeData>

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            AnimeEpisodeFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_anime_episode

    override fun initView() {

        episodeAdapter = buildAdapter<EpisodeData> {
            addItem<EpisodeData, ItemAnimeEpisodeBinding>(R.layout.item_anime_episode) {
                initView { data, _, _ ->
                    itemBinding.apply {
                        //提取剧集内容
                        val episodeInfo = getEpisodeInfo(data.episodeTitle)
                        episodeNumberTv.text = episodeInfo[0]
                        episodeTitleTv.text = episodeInfo[1]

                        //展示最后观看时间
                        lastWatchTv.isGone = data.lastWatched == null
                        lastWatchTv.text = data.lastWatched

                        itemLayout.setOnClickListener {
                            val episodeNum = getEpisodeNum(episodeInfo[0])
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

        arguments?.run {
            getParcelable<BangumiData>("bangumi_data")?.let {
                viewModel.setBangumiData(it)
            }
        }
    }

    private fun initObserver() {
        viewModel.episodeLiveData.observe(this) {
            dataBinding.episodeRv.setData(it)
        }

        viewModel.episodeSortLiveData.observe(this) { asc ->
            val color = if (asc) R.color.text_gray else R.color.text_blue

            val colorState = AppCompatResources.getColorStateList(mAttachActivity, color)
            ImageViewCompat.setImageTintList(dataBinding.sortIv, colorState)

            dataBinding.sortTv.text = if (asc) "正序" else "倒序"
            dataBinding.sortTv.setTextColorRes(color)

            episodeAdapter.items.reverse()
            episodeAdapter.notifyDataSetChanged()
        }
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
            return matcher.group().run { substring(1, length - 1) }
        }
        return ""
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
}