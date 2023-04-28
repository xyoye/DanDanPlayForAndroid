package com.xyoye.user_component.ui.fragment.scan_filter

import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.getFolderName
import com.xyoye.data_component.bean.FolderBean
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.FragmentScanFilterBinding
import com.xyoye.user_component.databinding.ItemFilterFolderBinding

class ScanFilterFragment : BaseFragment<ScanFilterFragmentViewModel, FragmentScanFilterBinding>() {

    companion object {
        fun newInstance() = ScanFilterFragment()
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            ScanFilterFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_scan_filter

    override fun initView() {

        dataBinding.filterFolderRv.apply {
            itemAnimator = null

            layoutManager = vertical()

            adapter = buildAdapter {
                addItem<FolderBean, ItemFilterFolderBinding>(R.layout.item_filter_folder) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            val fileCountText = "${data.fileCount}视频"

                            folderTv.text = getFolderName(data.folderPath)
                            fileCountTv.text = fileCountText

                            folderIv.setImageResource(if (data.isFilter) R.drawable.ic_folder_filter else R.drawable.ic_folder)
                            folderTv.setTextColorRes(if (data.isFilter) R.color.text_red else R.color.text_black)
                            fileCountTv.setTextColorRes(if (data.isFilter) R.color.text_red else R.color.text_gray)

                            filterFolderCb.setOnCheckedChangeListener(null)
                            filterFolderCb.isChecked = data.isFilter
                            filterFolderCb.setOnCheckedChangeListener { _, isChecked ->
                                viewModel.updateFolder(data.folderPath, isChecked)
                            }

                            itemLayout.setOnClickListener {
                                viewModel.updateFolder(data.folderPath, !data.isFilter)
                            }
                        }
                    }
                }
            }
        }

        viewModel.folderLiveData.observe(this) {
            dataBinding.filterFolderRv.setData(it)
        }
    }
}