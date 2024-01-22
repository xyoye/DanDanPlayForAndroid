package com.xyoye.local_component.ui.fragment.bind_danmu

import android.graphics.Typeface
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.setupHorizontalAnimation
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.extension.collectAtStarted
import com.xyoye.common_component.extension.ifNullOrBlank
import com.xyoye.common_component.extension.isInvalid
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.toFile
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.FileManagerDialog
import com.xyoye.data_component.data.DanmuAnimeData
import com.xyoye.data_component.data.DanmuEpisodeData
import com.xyoye.data_component.enums.FileManagerAction
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.FragmentBindDanmuSourceBinding
import com.xyoye.local_component.databinding.ItemDanmuContentBinding
import com.xyoye.local_component.databinding.ItemDanmuHeaderBinding
import com.xyoye.local_component.ui.activities.bind_source.BindExtraSourceActivity
import com.xyoye.local_component.ui.activities.bind_source.BindExtraSourceViewModel
import com.xyoye.local_component.ui.dialog.DanmuDownloadDialog

/**
 * Created by xyoye on 2022/1/25
 */
class BindDanmuSourceFragment :
    BaseFragment<BindDanmuSourceFragmentViewModel, FragmentBindDanmuSourceBinding>() {

    companion object {
        fun newInstance() = BindDanmuSourceFragment()
    }

    private var danmuDownloadDialog: DanmuDownloadDialog? = null

    private val parentViewModel: BindExtraSourceViewModel by viewModels(ownerProducer = { mAttachActivity })

    override fun getLayoutId() = R.layout.fragment_bind_danmu_source

    override fun initViewModel() = ViewModelInit(
        BR.viewModel,
        BindDanmuSourceFragmentViewModel::class.java
    )

    override fun initView() {
        viewModel.setStorageFile((activity as BindExtraSourceActivity).storageFile)

        initRv()

        initListener()

        viewModel.matchDanmu()
    }

    private fun initRv() {
        dataBinding.headerRv.apply {
            itemAnimator = null

            layoutManager = vertical()

            adapter = buildAdapter {
                addItem<DanmuAnimeData, ItemDanmuHeaderBinding>(R.layout.item_danmu_header) {
                    initView { data, _, _ ->
                        itemBinding.animeNameTv.text = data.animeTitle.ifNullOrBlank { "未知" }

                        // 绑定时，修改字体颜色
                        itemBinding.animeNameTv.isSelected = data.isBound

                        // 选中时，修改字体大小与粗细
                        val typeStyle = if (data.isSelected) Typeface.BOLD else Typeface.NORMAL
                        itemBinding.animeNameTv.setTypeface(Typeface.DEFAULT, typeStyle)

                        // 推荐时，显示推荐标签
                        itemBinding.viewRecommend.isVisible = data.isRecommend

                        itemBinding.itemLayout.setOnClickListener {
                            viewModel.selectAnime(data)
                        }
                    }
                }
            }
        }

        dataBinding.contentRv.apply {
            itemAnimator = null

            layoutManager = vertical()

            adapter = buildAdapter {
                setupHorizontalAnimation()

                addItem<DanmuEpisodeData, ItemDanmuContentBinding>(R.layout.item_danmu_content) {
                    initView { data, _, _ ->
                        itemBinding.episodeTv.text = data.episodeTitle
                        itemBinding.animeTitleTv.text = data.animeTitle
                        itemBinding.animeTitleTv.isVisible = data.isRecommend
                        itemBinding.episodeTv.isSelected = data.isBound
                        itemBinding.downloadIv.setOnClickListener {
                            viewModel.getDanmuThirdSource(data)
                        }
                        itemBinding.root.setOnClickListener {
                            viewModel.downloadDanmu(data)
                        }
                    }
                }
            }
        }
    }

    private fun initListener() {
        viewModel.danmuAnimeListFlow.collectAtStarted(this) {
            dataBinding.headerRv.isVisible = it.isNotEmpty()
            dataBinding.contentRv.isVisible = it.isNotEmpty()
            dataBinding.verticalDivider.isVisible = it.isNotEmpty()
            dataBinding.emptyCl.isVisible = it.isEmpty()
            dataBinding.headerRv.setData(it)
        }

        viewModel.danmuEpisodeListFlow.collectAtStarted(this) {
            dataBinding.contentRv.setData(it)
        }

        viewModel.downloadDialogShowFlow.collectAtStarted(this) { (episode, related) ->
            danmuDownloadDialog?.dismiss()
            danmuDownloadDialog = DanmuDownloadDialog(
                requireActivity(),
                episode,
                related,
                downloadOfficial = { viewModel.downloadDanmu(episode, it) },
                downloadRelated = { viewModel.downloadDanmu(episode, it) }
            )
            danmuDownloadDialog!!.show()
        }

        viewModel.downloadDialogDismissFlow.collectAtStarted(this) {
            danmuDownloadDialog?.dismiss()
        }

        parentViewModel.searchTextFlow.collectAtStarted(
            this,
            minActiveState = Lifecycle.State.RESUMED
        ) {
            viewModel.searchDanmu(it)
        }

        parentViewModel.storageFileFlow.collectAtStarted(this) {
            dataBinding.tvUnbindDanmu.isEnabled = it.playHistory?.danmuPath?.isNotEmpty() == true
        }

        dataBinding.tvUnbindDanmu.setOnClickListener {
            viewModel.unbindDanmu()
        }
        dataBinding.tvSelectLocalDanmu.setOnClickListener {
            selectLocalDanmuFile()
        }
    }

    private fun selectLocalDanmuFile() {
        FileManagerDialog(
            requireActivity(),
            FileManagerAction.ACTION_SELECT_DANMU
        ) {
            if (it.toFile().isInvalid()) {
                ToastCenter.showError("绑定弹幕失败，弹幕不存在或内容为空")
                return@FileManagerDialog
            }
            viewModel.bindLocalDanmu(it)
        }.show()
    }
}