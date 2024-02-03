package com.xyoye.local_component.ui.fragment.bind_subtitle

import android.text.TextUtils
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import com.xyoye.common_component.adapter.paging.BasePagingAdapter
import com.xyoye.common_component.adapter.paging.PagingFooterAdapter
import com.xyoye.common_component.adapter.paging.addItem
import com.xyoye.common_component.adapter.paging.buildPagingAdapter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.collectAtStarted
import com.xyoye.common_component.extension.isInvalid
import com.xyoye.common_component.extension.toFile
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.FileManagerDialog
import com.xyoye.data_component.data.SubtitleSourceBean
import com.xyoye.data_component.enums.FileManagerAction
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.FragmentBindSubtitleSourceBinding
import com.xyoye.local_component.databinding.ItemSubtitleSearchSourceBinding
import com.xyoye.local_component.ui.activities.bind_source.BindExtraSourceActivity
import com.xyoye.local_component.ui.activities.bind_source.BindExtraSourceViewModel
import com.xyoye.local_component.ui.dialog.ShooterSecretDialog
import com.xyoye.local_component.ui.dialog.SubtitleDetailDialog
import com.xyoye.local_component.ui.dialog.SubtitleFileListDialog


/**
 * Created by xyoye on 2022/1/25
 */
class BindSubtitleSourceFragment :
    BaseFragment<BindSubtitleSourceFragmentViewModel, FragmentBindSubtitleSourceBinding>() {

    companion object {
        fun newInstance() = BindSubtitleSourceFragment()
    }

    private val parentViewModel: BindExtraSourceViewModel by viewModels(ownerProducer = { mAttachActivity })

    private lateinit var subtitleAdapter: BasePagingAdapter<SubtitleSourceBean>

    override fun initViewModel() = ViewModelInit(
        BR.viewModel,
        BindSubtitleSourceFragmentViewModel::class.java
    )

    override fun getLayoutId() = R.layout.fragment_bind_subtitle_source

    override fun initView() {
        viewModel.storageFile = (activity as BindExtraSourceActivity).storageFile

        initRv()

        initListener()

        updateKeyActionView()

        viewModel.matchSubtitle()
    }

    private fun initRv() {
        subtitleAdapter = buildPagingAdapter {

            addItem<SubtitleSourceBean, ItemSubtitleSearchSourceBinding>(R.layout.item_subtitle_search_source) {
                initView { data, position, _ ->
                    itemBinding.apply {
                        val describe = if (data.isMatch) {
                            "来源: ${data.source}"
                        } else {
                            "语言: ${data.language}"
                        }
                        val positionText = (position + 1).toString()

                        positionTv.text = positionText
                        subtitleNameTv.text = data.name
                        subtitleDescribeTv.text = describe
                        itemLayout.setOnClickListener {
                            if (data.isMatch) {
                                viewModel.downloadSearchSubtitle(data.name, data.matchUrl)
                            } else {
                                viewModel.detailSearchSubtitle(data)
                            }
                        }
                    }
                }
            }
        }

        val contactAdapter = subtitleAdapter.withLoadStateFooter(
            PagingFooterAdapter { subtitleAdapter.retry() }
        )

        dataBinding.subtitleRv.apply {
            layoutManager = vertical()

            adapter = contactAdapter
        }
    }

    private fun initListener() {
        subtitleAdapter.addLoadStateListener {
            val emptyData = it.refresh is LoadState.NotLoading && subtitleAdapter.itemCount == 0
            dataBinding.emptyCl.isVisible = emptyData
            dataBinding.subtitleRv.isVisible = emptyData.not()
        }

        viewModel.subtitleSearchLiveData.observe(this) {
            subtitleAdapter.submitPagingData(lifecycle, it)
        }
        viewModel.subtitleMatchLiveData.observe(this) {
            subtitleAdapter.submitPagingData(lifecycle, it)
        }

        viewModel.searchSubtitleDetailLiveData.observe(this) {
            SubtitleDetailDialog(
                requireActivity(),
                it,
                downloadOne = {
                    SubtitleFileListDialog(
                        requireActivity(),
                        it.filelist!!
                    ) { fileName, url ->
                        viewModel.downloadSearchSubtitle(fileName, url)
                    }.show()
                },
                downloadZip = { fileName, url ->
                    viewModel.downloadSearchSubtitle(fileName, url, true)
                }
            ).show()
        }

        viewModel.unzipResultLiveData.observe(this) { dirPath ->
            FileManagerDialog(
                requireActivity(),
                FileManagerAction.ACTION_SELECT_SUBTITLE,
                dirPath
            ) {
                viewModel.databaseSubtitle(it)
                ToastCenter.showSuccess("绑定字幕成功！")
            }.show()
        }

        parentViewModel.searchTextFlow.collectAtStarted(
            this,
            minActiveState = Lifecycle.State.RESUMED
        ) {
            val shooterSecret = SubtitleConfig.getShooterSecret()
            if (shooterSecret.isNullOrEmpty()) {
                settingSubtitleKey()
            } else {
                viewModel.searchSubtitle(it)
            }
        }

        parentViewModel.storageFileFlow.collectAtStarted(this) {
            val boundSubtitle = it.playHistory?.subtitlePath?.isNotEmpty() == true
            dataBinding.tvUnbindSubtitle.isEnabled = boundSubtitle
        }

        dataBinding.tvUnbindSubtitle.setOnClickListener {
            viewModel.unbindSubtitle()
        }
        dataBinding.tvSelectLocalSubtitle.setOnClickListener {
            selectLocalSubtitleFile()
        }
        dataBinding.tvSettingSubtitleKey.setOnClickListener {
            settingSubtitleKey()
        }
    }

    private fun settingSubtitleKey() {
        val dialog = ShooterSecretDialog(requireActivity())
        dialog.setOnDismissListener { updateKeyActionView() }
        dialog.show()
    }

    private fun updateKeyActionView() {
        dataBinding.tvSettingSubtitleKey.isSelected =
            TextUtils.isEmpty(SubtitleConfig.getShooterSecret())
    }

    private fun selectLocalSubtitleFile() {
        FileManagerDialog(
            requireActivity(),
            FileManagerAction.ACTION_SELECT_SUBTITLE
        ) {
            if (it.toFile().isInvalid()) {
                ToastCenter.showError("绑定字幕失败，字幕不存在或内容为空")
                return@FileManagerDialog
            }
            viewModel.databaseSubtitle(it)
        }.show()
    }
}