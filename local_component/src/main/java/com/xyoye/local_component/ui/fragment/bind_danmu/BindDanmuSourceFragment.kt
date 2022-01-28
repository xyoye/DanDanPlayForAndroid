package com.xyoye.local_component.ui.fragment.bind_danmu

import android.os.Bundle
import android.text.TextUtils
import androidx.core.view.isVisible
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.dialog.FileManagerDialog
import com.xyoye.data_component.bean.DanmuSourceBean
import com.xyoye.data_component.bean.DanmuSourceContentBean
import com.xyoye.data_component.bean.DanmuSourceHeaderBean
import com.xyoye.data_component.enums.FileManagerAction
import com.xyoye.data_component.enums.MediaType
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.FragmentBindDanmuSourceBinding
import com.xyoye.local_component.databinding.ItemDanmuContentBinding
import com.xyoye.local_component.databinding.ItemDanmuHeaderBinding
import com.xyoye.local_component.listener.ExtraSourceListener
import com.xyoye.local_component.ui.activities.bind_source.BindExtraSourceActivity
import com.xyoye.local_component.ui.dialog.DanmuDownloadDialog

/**
 * Created by xyoye on 2022/1/25
 */
class BindDanmuSourceFragment :
    BaseFragment<BindDanmuSourceFragmentViewModel, FragmentBindDanmuSourceBinding>(),
    ExtraSourceListener {

    private var danmuDownloadDialog: DanmuDownloadDialog? = null

    companion object {
        private const val TAG_VIDEO_PATH = "tag_video_path"
        private const val TAG_UNIQUE_KEY = "tag_unique_key"
        private const val TAG_MEDIA_TYPE = "tag_media_type"

        fun newInstance(
            videoPath: String?,
            uniqueKey: String,
            mediaType: String
        ): BindDanmuSourceFragment {
            val args = Bundle()
            args.putString(TAG_VIDEO_PATH, videoPath)
            args.putString(TAG_UNIQUE_KEY, uniqueKey)
            args.putString(TAG_MEDIA_TYPE, mediaType)
            val fragment = BindDanmuSourceFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_bind_danmu_source

    override fun initViewModel() = ViewModelInit(
        BR.viewModel,
        BindDanmuSourceFragmentViewModel::class.java
    )

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

        if (TextUtils.isEmpty(videoPath).not()) {
            viewModel.matchDanmu(videoPath!!)
        }
    }

    private fun initRv() {
        dataBinding.headerRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addItem<DanmuSourceHeaderBean, ItemDanmuHeaderBinding>(R.layout.item_danmu_header) {
                    initView { data, position, _ ->
                        itemBinding.animeNameTv.text = data.animeName
                        itemBinding.itemLayout.isSelected = data.isSelected
                        itemBinding.animeNameTv.isSelected = data.isLoaded
                        itemBinding.recommendView.isVisible = data.isRecommend
                        itemBinding.itemLayout.setOnClickListener {
                            viewModel.selectTab(position)
                        }
                    }
                }
            }
        }

        dataBinding.contentRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {

                addItem<DanmuSourceContentBean, ItemDanmuContentBinding>(R.layout.item_danmu_content) {
                    initView { data, _, _ ->
                        itemBinding.episodeTv.text = data.episodeTitle
                        itemBinding.episodeTv.isSelected = data.isLoaded
                        itemBinding.recommendView.isVisible = data.isRecommend
                        itemBinding.root.setOnClickListener {
                            if (DanmuConfig.isShowThirdSource()) {
                                viewModel.getDanmuThirdSource(data)
                            } else {
                                viewModel.downloadDanmu(data)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initObserver() {
        viewModel.danmuHeaderLiveData.observe(this) {
            dataBinding.headerRv.isVisible = it.isNotEmpty()
            dataBinding.contentRv.isVisible = it.isNotEmpty()
            dataBinding.emptyCl.isVisible = it.isEmpty()
            dataBinding.headerRv.setData(it)
        }

        viewModel.danmuContentLiveData.observe(this) {
            dataBinding.contentRv.setData(it)
        }

        viewModel.thirdSourceLiveData.observe(this) {
            danmuDownloadDialog?.dismiss()
            danmuDownloadDialog = DanmuDownloadDialog(
                it.first.episodeId,
                it.second
            ) { sources: MutableList<DanmuSourceBean>, isCheckedAll: Boolean ->
                viewModel.downloadDanmu(sources, isCheckedAll, it.first)
            }
            danmuDownloadDialog!!.show(this)
        }

        viewModel.sourceRefreshLiveData.observe(this) {
            danmuDownloadDialog?.dismiss()
            (mAttachActivity as BindExtraSourceActivity).onSourceChanged()
        }
    }

    override fun search(searchText: String) {
        viewModel.searchDanmu(searchText)
    }

    override fun setting() {
        ARouter.getInstance()
            .build(RouteTable.User.SettingDanmuSource)
            .navigation()
    }

    override fun localFile() {
        FileManagerDialog(
            FileManagerAction.ACTION_SELECT_DANMU
        ) {
            viewModel.bindLocalDanmu(it)
        }.show(this)
    }

    override fun unbindDanmu() {
        viewModel.unbindDanmu()
    }

    override fun unbindSubtitle() {

    }
}