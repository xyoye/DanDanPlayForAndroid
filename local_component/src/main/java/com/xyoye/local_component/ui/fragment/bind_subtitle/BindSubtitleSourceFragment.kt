package com.xyoye.local_component.ui.fragment.bind_subtitle

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.paging.LoadState
import com.xyoye.common_component.adapter.paging.BasePagingAdapter
import com.xyoye.common_component.adapter.paging.PagingFooterAdapter
import com.xyoye.common_component.adapter.paging.addItem
import com.xyoye.common_component.adapter.paging.buildPagingAdapter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.FileManagerDialog
import com.xyoye.data_component.data.SubtitleSourceBean
import com.xyoye.data_component.enums.FileManagerAction
import com.xyoye.data_component.enums.MediaType
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.FragmentBindSubtitleSourceBinding
import com.xyoye.local_component.databinding.ItemSubtitleSearchSourceBinding
import com.xyoye.local_component.listener.ExtraSourceListener
import com.xyoye.local_component.ui.activities.bind_source.BindExtraSourceActivity
import com.xyoye.local_component.ui.dialog.ShooterSecretDialog
import com.xyoye.local_component.ui.dialog.SubtitleDetailDialog
import com.xyoye.local_component.ui.dialog.SubtitleFileListDialog


/**
 * Created by xyoye on 2022/1/25
 */
class BindSubtitleSourceFragment :
    BaseFragment<BindSubtitleSourceFragmentViewModel, FragmentBindSubtitleSourceBinding>(),
    ExtraSourceListener {

    private lateinit var subtitleAdapter: BasePagingAdapter<SubtitleSourceBean>

    companion object {
        private const val TAG_VIDEO_PATH = "tag_video_path"
        private const val TAG_UNIQUE_KEY = "tag_unique_key"
        private const val TAG_MEDIA_TYPE = "tag_media_type"

        fun newInstance(
            videoPath: String?,
            uniqueKey: String,
            mediaType: String
        ): BindSubtitleSourceFragment {
            val args = Bundle()
            args.putString(TAG_VIDEO_PATH, videoPath)
            args.putString(TAG_UNIQUE_KEY, uniqueKey)
            args.putString(TAG_MEDIA_TYPE, mediaType)
            val fragment = BindSubtitleSourceFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initViewModel() = ViewModelInit(
        BR.viewModel,
        BindSubtitleSourceFragmentViewModel::class.java
    )

    override fun getLayoutId() = R.layout.fragment_bind_subtitle_source

    override fun initView() {
        val videoPath: String? = arguments?.getString(TAG_VIDEO_PATH)
        val uniqueKey = arguments?.getString(TAG_UNIQUE_KEY)
        val mediaTypeValue = arguments?.getString(TAG_MEDIA_TYPE)

        if (uniqueKey.isNullOrEmpty() || mediaTypeValue.isNullOrEmpty())
            return

        viewModel.uniqueKey = uniqueKey
        viewModel.mediaType = MediaType.fromValue(mediaTypeValue)
        viewModel.videoPath = videoPath

        initRv()

        initObserver()

        videoPath?.let { viewModel.matchSubtitle(it) }
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

    private fun initObserver() {
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
            SubtitleDetailDialog(it,
                downloadOne = {
                    SubtitleFileListDialog(it.filelist!!) { fileName, url ->
                        viewModel.downloadSearchSubtitle(fileName, url)
                    }.show(this)
                },
                downloadZip = { fileName, url ->
                    viewModel.downloadSearchSubtitle(fileName, url, true)
                }
            ).show(this)
        }

        viewModel.sourceRefreshLiveData.observe(this) {
            (mAttachActivity as BindExtraSourceActivity).onSourceChanged()
        }

        viewModel.unzipResultLiveData.observe(this) { dirPath ->
            FileManagerDialog(FileManagerAction.ACTION_SELECT_SUBTITLE, dirPath) {
                viewModel.databaseSubtitle(it)
                ToastCenter.showSuccess("绑定字幕成功！")
            }.show(this)
        }
    }

    override fun search(searchText: String) {
        val shooterSecret = SubtitleConfig.getShooterSecret()
        if (shooterSecret.isNullOrEmpty()) {
            setting()
        } else {
            viewModel.searchSubtitle(searchText)
        }
    }

    override fun setting() {
        ShooterSecretDialog().show(this)
    }

    override fun localFile() {
        FileManagerDialog(
            FileManagerAction.ACTION_SELECT_SUBTITLE
        ) {
            viewModel.databaseSubtitle(it)
        }.show(this)
    }

    override fun unbindDanmu() {

    }

    override fun unbindSubtitle() {
        viewModel.unbindSubtitle()
    }
}