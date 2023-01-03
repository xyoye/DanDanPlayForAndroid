package com.xyoye.anime_component.ui.fragment.search_magnet

import android.os.Bundle
import androidx.core.view.isVisible
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.anime_component.BR
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.FragmentSearchMagnetBinding
import com.xyoye.anime_component.databinding.ItemSearchMagnetBinding
import com.xyoye.anime_component.listener.SearchListener
import com.xyoye.anime_component.ui.activities.search.SearchActivity
import com.xyoye.anime_component.ui.dialog.MagnetScreenDialog
import com.xyoye.anime_component.ui.dialog.SearchDomainDialog
import com.xyoye.common_component.adapter.addEmptyView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.addToClipboard
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.MagnetUtils
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.data.MagnetData
import com.xyoye.data_component.enums.MagnetScreenType


class SearchMagnetFragment :
    BaseFragment<SearchMagnetFragmentViewModel, FragmentSearchMagnetBinding>(), SearchListener {

    companion object {
        fun newInstance(searchWord: String?): SearchMagnetFragment {
            val argument = Bundle()
            argument.putString("search_word", searchWord)
            val searchMagnetFragment = SearchMagnetFragment()
            searchMagnetFragment.arguments = argument
            return searchMagnetFragment
        }
    }

    private val actionData = ManageMagnet.values().map { it.toSheetActionBean() }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            SearchMagnetFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_search_magnet

    override fun initView() {
        initObserver()

        initRv()

        dataBinding.historyLabelsView.setOnLabelClickListener { _, _, position ->
            viewModel.searchHistoryLiveData.value?.let {
                search(it[position])
            }
        }

        dataBinding.historyLabelsView.setOnLabelLongClickListener { _, _, position ->
            viewModel.searchHistoryLiveData.value?.let {
                viewModel.deleteSearchHistory(it[position])
                return@setOnLabelLongClickListener true
            }
            return@setOnLabelLongClickListener false
        }

        dataBinding.domainTv.setOnClickListener {
            showInputDomainDialog()
        }

        val isResDomainExist = AppConfig.getMagnetResDomain() != null
        dataBinding.domainTv.setTextColorRes(if (isResDomainExist) R.color.text_theme else R.color.text_red)

        arguments?.apply {
            val searchWord = getString("search_word")
            if (!searchWord.isNullOrEmpty()) {
                search(searchWord)
            }
        }
    }

    private fun initRv() {
        dataBinding.magnetRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addEmptyView(R.layout.layout_empty)

                addItem<MagnetData, ItemSearchMagnetBinding>(R.layout.item_search_magnet) {
                    initView { data, _, _ ->
                        itemBinding.apply {

                            val time = data.PublishDate?.run {
                                val spaceIndex = indexOf(" ")
                                if (spaceIndex > 0) {
                                    substring(0, spaceIndex)
                                } else {
                                    this
                                }
                            }

                            magnetTitleTv.text = data.Title
                            magnetSizeTv.text = data.FileSize
                            magnetSubgroupTv.text = data.SubgroupName
                            magnetTypeTv.text = data.TypeName
                            magnetTimeTv.text = time

                            contentView.setOnClickListener {
                                val magnetHash = MagnetUtils.getMagnetHash(data.Magnet)
                                if (magnetHash.isEmpty()){
                                    ToastCenter.showError("错误，磁链为空或无法解析")
                                    return@setOnClickListener
                                }
                                val magnetLink = "magnet:?xt=urn:btih:$magnetHash"
                                ARouter.getInstance()
                                    .build(RouteTable.Download.PlaySelection)
                                    .withString("magnetLink", magnetLink)
                                    .navigation()
                            }

                            contentView.setOnLongClickListener {
                                showActionDialog(data)
                                return@setOnLongClickListener true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initObserver() {
        viewModel.magnetLiveData.observe(this) {
            dataBinding.magnetRv.setData(it)
        }

        viewModel.searchHistoryLiveData.observe(this) {
            dataBinding.historyLabelsView.setLabels(it)
        }

        viewModel.magnetTypeData.observe(this) {
            MagnetScreenDialog(mAttachActivity, it, MagnetScreenType.TYPE) { entity ->
                viewModel.magnetTypeId.set(entity.screenId)
                viewModel.magnetTypeText.set(entity.screenName)
                viewModel.search()
            }.show()
        }

        viewModel.magnetSubgroupData.observe(this) {
            MagnetScreenDialog(mAttachActivity, it, MagnetScreenType.SUBGROUP) { entity ->
                viewModel.magnetSubgroupId.set(entity.screenId)
                viewModel.magnetSubgroupText.set(entity.screenName)
                viewModel.search()
            }.show()
        }

        viewModel.domainErrorLiveData.observe(this) {
            ToastCenter.showError("请完善节点信息")
            showInputDomainDialog()
        }
    }

    override fun search(searchText: String) {
        dataBinding.historyCl.isVisible = false
        dataBinding.magnetRv.isVisible = true

        (mAttachActivity as SearchActivity).onSearch(searchText)

        viewModel.searchText.set(searchText)
        viewModel.search()
    }

    override fun onTextClear() {
        dataBinding.historyCl.isVisible = true
        dataBinding.magnetRv.isVisible = false
    }

    private fun showInputDomainDialog() {
        SearchDomainDialog(requireActivity()) {
            dataBinding.domainTv.setTextColorRes(R.color.text_theme)
            AppConfig.putMagnetResDomain(it)
        }.show()
    }

    private fun showActionDialog(data: MagnetData) {
        BottomActionDialog(requireActivity(), actionData) {
            val magnetHash = MagnetUtils.getMagnetHash(data.Magnet)
            if (data.Magnet.isNullOrEmpty()) {
                ToastCenter.showError("磁链信息为空，无法复制")
                return@BottomActionDialog true
            }
            if (it.actionId == ManageMagnet.Copy && magnetHash.isEmpty()) {
                ToastCenter.showError("错误，磁链为空或无法解析")
                return@BottomActionDialog true
            }
            when (it.actionId) {
                ManageMagnet.Copy -> {
                    "magnet:?xt=urn:btih:$magnetHash".addToClipboard()
                    ToastCenter.showSuccess("磁链已复制！")
                }
                ManageMagnet.CopyContent -> {
                    data.Magnet?.addToClipboard()
                    ToastCenter.showSuccess("磁链信息已复制！")
                }
            }
            return@BottomActionDialog true
        }.show()
    }

    private enum class ManageMagnet(val title: String, val icon: Int) {
        Copy("复制磁链", R.drawable.ic_magnet_copy),
        CopyContent("复制完整磁链", R.drawable.ic_magnet_copy_content);

        fun toSheetActionBean() = SheetActionBean(this, title, icon)
    }
}