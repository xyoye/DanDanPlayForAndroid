package com.xyoye.local_component.ui.dialog

import android.app.Activity
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.setupDiffUtil
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.getDomainFormUrl
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.bean.DanmuSourceBean
import com.xyoye.data_component.data.DanmuEpisodeData
import com.xyoye.data_component.data.DanmuRelatedUrlData
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogDanmuDowanloadBinding
import com.xyoye.local_component.databinding.ItemDanmuSourceSelectBinding

/**
 * Created by xyoye on 2020/11/25.
 */

class DanmuDownloadDialog(
    activity: Activity,
    private val episode: DanmuEpisodeData,
    private val relatedData: List<DanmuRelatedUrlData>,
    private val downloadRelated: (List<DanmuRelatedUrlData>) -> Unit,
    private val downloadOfficial: (withRelated: Boolean) -> Unit
) : BaseBottomDialog<DialogDanmuDowanloadBinding>(activity) {

    private val danmuSources: MutableList<DanmuSourceBean> = generateDanmuSources()

    override fun getChildLayoutId() = R.layout.dialog_danmu_dowanload

    override fun initView(binding: DialogDanmuDowanloadBinding) {
        disableSheetDrag()

        setTitle("下载弹幕")

        binding.downloadPathTv.text = PathHelper.getDanmuDirectory().absolutePath

        initRadioGroup(binding)

        initRecyclerView(binding)

        setNegativeListener { dismiss() }

        setPositiveListener {
            // 选中所有源
            if (binding.rbAllSource.isChecked) {
                downloadOfficial.invoke(true)
                return@setPositiveListener
            }

            // 勾选了官方源和所有第三方源都勾选了
            val checkedSources = danmuSources.filter { it.isChecked }
            if (checkedSources.size == danmuSources.size) {
                downloadOfficial.invoke(true)
                return@setPositiveListener
            }

            // 只勾选了官方源
            if (checkedSources.size == 1 && checkedSources.first().isOfficial) {
                downloadOfficial.invoke(false)
                return@setPositiveListener
            }

            // 勾选了部分源
            downloadRelated.invoke(checkedSources.map { DanmuRelatedUrlData(it.sourceUrl) })
        }
    }

    private fun initRadioGroup(binding: DialogDanmuDowanloadBinding) {
        binding.rgSource.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.rbAllSource.id) {
                binding.danmuSourceRv.setData(emptyList())
            } else {
                binding.danmuSourceRv.setData(danmuSources)
            }
        }
    }

    private fun initRecyclerView(binding: DialogDanmuDowanloadBinding) {
        binding.danmuSourceRv.apply {
            itemAnimator = null
            layoutManager = vertical()

            adapter = buildAdapter {

                setupDiffUtil {
                    newDataInstance { it }
                    areItemsTheSame { old, new ->
                        (old as DanmuSourceBean).sourceUrl == (new as DanmuSourceBean).sourceUrl
                    }
                }

                addItem<DanmuSourceBean, ItemDanmuSourceSelectBinding>(R.layout.item_danmu_source_select) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            danmuSourceCb.isChecked = data.isChecked
                            danmuSourceCb.text = data.sourceName
                            danmuSourceDescribeTv.text = data.sourceDescribe
                        }

                        itemBinding.root.setOnClickListener {
                            selectSource(data, binding)
                        }
                    }
                }
            }
        }
    }

    private fun selectSource(source: DanmuSourceBean, binding: DialogDanmuDowanloadBinding) {
        val newSources = danmuSources.map {
            if (it.sourceUrl == source.sourceUrl) {
                it.copy(isChecked = it.isChecked.not())
            } else {
                it
            }
        }

        danmuSources.clear()
        danmuSources.addAll(newSources)
        binding.danmuSourceRv.setData(newSources)
    }

    private fun generateDanmuSources(): MutableList<DanmuSourceBean> {
        val downloadSources = mutableListOf<DanmuSourceBean>()
        //弹弹play源
        DanmuSourceBean(
            "弹弹Play",
            episode.episodeId.toString(),
            "www.dandanplay.com",
            isOfficial = true,
            isChecked = true,
            format = 0
        ).let {
            downloadSources.add(it)
        }

        //第三方源
        relatedData.map {
            DanmuSourceBean(getDomainFormUrl(it.url), it.url, it.url)
        }.let {
            downloadSources.addAll(it)
        }

        return downloadSources
    }
}