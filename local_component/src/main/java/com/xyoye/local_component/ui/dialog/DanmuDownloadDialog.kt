package com.xyoye.local_component.ui.dialog

import androidx.core.view.isVisible
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.initData
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.getDomainFormUrl
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.bean.DanmuSourceBean
import com.xyoye.data_component.data.DanmuMatchDetailData
import com.xyoye.data_component.data.DanmuRelatedData
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogDanmuDowanloadBinding
import com.xyoye.local_component.databinding.ItemDanmuSourceSelectBinding

/**
 * Created by xyoye on 2020/11/25.
 */

class DanmuDownloadDialog : BaseBottomDialog<DialogDanmuDowanloadBinding> {

    private lateinit var matchData: DanmuMatchDetailData
    private lateinit var relatedData: DanmuRelatedData
    private lateinit var callback: (MutableList<DanmuSourceBean>, Boolean) -> Unit

    constructor() : super()

    constructor(
        matchData: DanmuMatchDetailData,
        relatedData: DanmuRelatedData,
        callback: (MutableList<DanmuSourceBean>, Boolean) -> Unit
    ) : super(true) {
        this.matchData = matchData
        this.relatedData = relatedData
        this.callback = callback
    }

    override fun getChildLayoutId() = R.layout.dialog_danmu_dowanload

    override fun initView(binding: DialogDanmuDowanloadBinding) {
        val downloadSources = initDownloadSources()

        setTitle("下载弹幕")

        binding.animeNameTv.text = matchData.animeTitle
        binding.episodeTv.text = matchData.episodeTitle

        binding.downloadPathTv.text = PathHelper.getDanmuDirectory().absolutePath

        binding.animeNameTv.setOnClickListener {
            ARouter.getInstance()
                .build(RouteTable.Anime.AnimeDetail)
                .withInt("animeId", matchData.animeId)
                .navigation()
        }

        setNegativeListener { dismiss() }

        setPositiveListener {
            val needDownloadUrls = mutableListOf<DanmuSourceBean>()
            for (sourceBean in downloadSources) {
                if (sourceBean.isChecked) {
                    needDownloadUrls.add(sourceBean)
                }
            }

            callback.invoke(needDownloadUrls, needDownloadUrls == downloadSources)
        }

        binding.danmuSourceRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter<DanmuSourceBean> {
                initData(downloadSources)

                addItem<DanmuSourceBean, ItemDanmuSourceSelectBinding>(R.layout.item_danmu_source_select) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            danmuSourceCb.isChecked = data.isChecked
                            danmuSourceCb.text = data.sourceName
                            danmuSourceDescribeTv.text = data.sourceDescribe
                            danmuFormatRg.isVisible = data.isOfficial
                            danmuFormatRg.setOnCheckedChangeListener { _, checkedId ->
                                data.format = when (checkedId) {
                                    R.id.danmu_simplified_rb -> 1
                                    R.id.danmu_traditional_rb -> 2
                                    else -> 0
                                }
                            }
                            danmuSourceCb.setOnCheckedChangeListener { _, isChecked ->
                                data.isChecked = isChecked
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initDownloadSources(): MutableList<DanmuSourceBean> {
        val downloadSources = mutableListOf<DanmuSourceBean>()
        //弹弹play源
        downloadSources.add(
            DanmuSourceBean(
                "弹弹Play",
                matchData.episodeId.toString(),
                "www.dandanplay.com",
                isOfficial = true,
                isChecked = true,
                format = 0
            )
        )

        //第三方源
        for (related in relatedData.relateds) {
            val url = related.url ?: continue
            downloadSources.add(DanmuSourceBean(getDomainFormUrl(url), url, url))
        }

        return downloadSources
    }
}