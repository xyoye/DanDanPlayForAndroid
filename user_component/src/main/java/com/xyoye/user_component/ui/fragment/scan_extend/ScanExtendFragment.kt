package com.xyoye.user_component.ui.fragment.scan_extend

import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.extension.setAutoSizeText
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.FastClickFilter
import com.xyoye.common_component.utils.getFolderName
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.common_component.weight.dialog.FileManagerDialog
import com.xyoye.data_component.entity.ExtendFolderEntity
import com.xyoye.data_component.enums.FileManagerAction
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.*

class ScanExtendFragment : BaseFragment<ScanExtendFragmentViewModel, FragmentScanExtendBinding>() {

    companion object {
        fun newInstance() = ScanExtendFragment()
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            ScanExtendFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_scan_extend

    override fun initView() {

        dataBinding.extendFolderRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter<Any> {
                addItem<Any, ItemExtendFolderAddBinding>(R.layout.item_extend_folder_add) {
                    checkType { data, _ -> data is Int }

                    initView { _, _, _ ->
                        itemBinding.itemLayout.setOnClickListener {
                            if (FastClickFilter.isNeedFilter())
                                return@setOnClickListener
                            showExtendFolderDialog()
                        }
                    }
                }

                addItem<Any, ItemExtendFolderBinding>(R.layout.item_extend_folder) {
                    checkType { data, _ -> data is ExtendFolderEntity }

                    initView { data, _, _ ->
                        data as ExtendFolderEntity
                        itemBinding.apply {
                            val fileCountText = "${data.childCount}视频"

                            folderTv.setAutoSizeText(getFolderName(data.folderPath))
                            fileCountTv.text = fileCountText

                            removeFolderIv.setOnClickListener {
                                if (FastClickFilter.isNeedFilter())
                                    return@setOnClickListener
                                showConfirmRemoveDialog(data)
                            }
                        }
                    }
                }

            }
        }

        viewModel.extendFolderLiveData.observe(this) {
            dataBinding.extendFolderRv.setData(it)
        }

        viewModel.getExtendFolder()
    }

    private fun showExtendFolderDialog() {
        FileManagerDialog(FileManagerAction.ACTION_SELECT_DIRECTORY) {
            viewModel.addExtendFolder(it)
        }.show(this)
    }

    private fun showConfirmRemoveDialog(entity: ExtendFolderEntity) {
        CommonDialog.Builder().apply {
            content = "确认移除文件夹？"
            addPositive {
                it.dismiss()
                viewModel.removeExtendFolder(entity)
            }
            addNegative { it.dismiss() }
        }.build().show(this)
    }
}