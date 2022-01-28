package com.xyoye.local_component.ui.dialog

import androidx.core.view.isVisible
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.getDomainFormUrl
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.bean.DanmuSourceBean
import com.xyoye.data_component.data.DanmuRelatedData
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogDanmuDowanloadBinding
import com.xyoye.local_component.databinding.ItemDanmuSourceSelectBinding

/**
 * Created by xyoye on 2020/11/25.
 */

class DanmuDownloadDialog : BaseBottomDialog<DialogDanmuDowanloadBinding> {

    private lateinit var relatedData: DanmuRelatedData
    private lateinit var callback: (MutableList<DanmuSourceBean>, Boolean) -> Unit
    private var episodeId: Int = 0

    private val languageViewIds =
        arrayOf(R.id.danmu_default_rb, R.id.danmu_simplified_rb, R.id.danmu_traditional_rb)

    constructor() : super()

    constructor(
        episodeId: Int,
        relatedData: DanmuRelatedData,
        callback: (MutableList<DanmuSourceBean>, Boolean) -> Unit
    ) : super(true) {
        this.episodeId = episodeId
        this.relatedData = relatedData
        this.callback = callback
    }

    override fun getChildLayoutId() = R.layout.dialog_danmu_dowanload

    override fun initView(binding: DialogDanmuDowanloadBinding) {
        val downloadSources = initDownloadSources()

        setTitle("下载弹幕")

        binding.downloadPathTv.text = PathHelper.getDanmuDirectory().absolutePath

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

            adapter = buildAdapter {

                addItem<DanmuSourceBean, ItemDanmuSourceSelectBinding>(R.layout.item_danmu_source_select) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            danmuSourceCb.isChecked = data.isChecked
                            danmuSourceCb.text = data.sourceName
                            danmuSourceDescribeTv.text = data.sourceDescribe
                            danmuFormatRg.isVisible = data.isOfficial

                            val checkedIndex = DanmuConfig.getDefaultLanguage()
                            danmuFormatRg.check(getLanguageViewId(checkedIndex))

                            danmuFormatRg.setOnCheckedChangeListener { _, checkedId ->
                                val languageType = languageViewIds.indexOf(checkedId)
                                data.format = languageType
                                DanmuConfig.putDefaultLanguage(languageType)
                            }
                            danmuSourceCb.setOnCheckedChangeListener { _, isChecked ->
                                data.isChecked = isChecked
                            }
                        }
                    }
                }
            }

            setData(downloadSources)
        }
    }

    private fun initDownloadSources(): MutableList<DanmuSourceBean> {
        val downloadSources = mutableListOf<DanmuSourceBean>()
        //弹弹play源
        downloadSources.add(
            DanmuSourceBean(
                "弹弹Play",
                episodeId.toString(),
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

    private fun getLanguageViewId(index: Int): Int {
        return languageViewIds.getOrNull(index) ?: R.id.danmu_default_rb
    }
}