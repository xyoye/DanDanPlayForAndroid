package com.xyoye.local_component.ui.fragment.bind_danmu

import android.graphics.Typeface
import android.util.TypedValue
import androidx.core.view.isVisible
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.setupHorizontalAnimation
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.isInvalid
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.toFile
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.FileManagerDialog
import com.xyoye.data_component.bean.DanmuSourceBean
import com.xyoye.data_component.bean.DanmuSourceContentBean
import com.xyoye.data_component.bean.DanmuSourceHeaderBean
import com.xyoye.data_component.enums.FileManagerAction
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
class BindDanmuSourceFragment : BaseFragment<BindDanmuSourceFragmentViewModel, FragmentBindDanmuSourceBinding>(),
    ExtraSourceListener {

    private var danmuDownloadDialog: DanmuDownloadDialog? = null

    companion object {
        fun newInstance() = BindDanmuSourceFragment()
    }

    override fun getLayoutId() = R.layout.fragment_bind_danmu_source

    override fun initViewModel() = ViewModelInit(
        BR.viewModel,
        BindDanmuSourceFragmentViewModel::class.java
    )

    override fun initView() {
        viewModel.storageFile = (activity as BindExtraSourceActivity).storageFile

        initActionView()

        initRv()

        initListener()

        viewModel.matchDanmu()
    }

    private fun initActionView() {
        val boundDanmu = viewModel.storageFile.playHistory?.danmuPath?.isNotEmpty() == true
        dataBinding.tvUnbindDanmu.isEnabled = boundDanmu
    }

    private fun initRv() {
        dataBinding.headerRv.apply {
            itemAnimator = null

            layoutManager = vertical()

            adapter = buildAdapter {
                addItem<DanmuSourceHeaderBean, ItemDanmuHeaderBinding>(R.layout.item_danmu_header) {
                    initView { data, position, _ ->
                        val animeNameSize = if (data.isRecommend) 14f else 13f
                        val typeStyle = if (data.isRecommend) Typeface.BOLD else Typeface.NORMAL
                        itemBinding.animeNameTv.text = data.animeName
                        itemBinding.animeNameTv.isSelected = data.isSelected
                        itemBinding.animeNameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, animeNameSize)
                        itemBinding.animeNameTv.setTypeface(Typeface.DEFAULT, typeStyle)
                        itemBinding.viewRecommend.isVisible = data.isRecommend
                        itemBinding.itemLayout.setOnClickListener {
                            viewModel.selectTab(position)
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

                addItem<DanmuSourceContentBean, ItemDanmuContentBinding>(R.layout.item_danmu_content) {
                    initView { data, _, _ ->
                        itemBinding.episodeTv.text = data.episodeTitle
                        itemBinding.animeTitleTv.text = data.animeTitle
                        itemBinding.animeTitleTv.isVisible = data.isRecommend
                        itemBinding.episodeTv.isSelected = data.isLoaded
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

    private fun initListener() {
        viewModel.danmuHeaderLiveData.observe(this) {
            dataBinding.headerRv.isVisible = it.isNotEmpty()
            dataBinding.contentRv.isVisible = it.isNotEmpty()
            dataBinding.verticalDivider.isVisible = it.isNotEmpty()
            dataBinding.emptyCl.isVisible = it.isEmpty()
            dataBinding.headerRv.setData(it)
        }

        viewModel.danmuContentLiveData.observe(this) {
            dataBinding.contentRv.setData(it)
        }

        viewModel.thirdSourceLiveData.observe(this) {
            danmuDownloadDialog?.dismiss()
            danmuDownloadDialog = DanmuDownloadDialog(
                requireActivity(),
                it.first.episodeId,
                it.second
            ) { sources: MutableList<DanmuSourceBean>, isCheckedAll: Boolean ->
                viewModel.downloadDanmu(sources, isCheckedAll, it.first)
            }
            danmuDownloadDialog!!.show()
        }

        viewModel.sourceRefreshLiveData.observe(this) {
            danmuDownloadDialog?.dismiss()
            (mAttachActivity as BindExtraSourceActivity).onSourceChanged()
        }

        dataBinding.tvUnbindDanmu.setOnClickListener {
            viewModel.unbindDanmu()
        }
        dataBinding.tvSelectLocalDanmu.setOnClickListener {
            selectLocalDanmuFile()
        }
        dataBinding.tvSettingDanmuSource.setOnClickListener {
            settingDanmuSource()
        }
    }

    override fun search(searchText: String) {
        viewModel.searchDanmu(searchText)
    }

    override fun onStorageFileChanged(storageFile: StorageFile) {
        viewModel.storageFile = storageFile
        initActionView()
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

    private fun settingDanmuSource() {
        ARouter.getInstance()
            .build(RouteTable.User.SettingDanmuSource)
            .navigation()
    }
}