package com.xyoye.user_component.ui.fragment.scan_extend

import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseFragment
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
import com.xyoye.user_component.databinding.FragmentScanExtendBinding
import com.xyoye.user_component.databinding.ItemExtendFolderAddBinding
import com.xyoye.user_component.databinding.ItemExtendFolderBinding

class ScanExtendFragment : BaseFragment<ScanExtendFragmentViewModel, FragmentScanExtendBinding>() {

    companion object {
        fun newInstance() = ScanExtendFragment()
    }

    private var fileManagerDialog: FileManagerDialog? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            ScanExtendFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_scan_extend

    override fun initView() {

        dataBinding.extendFolderRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addItem<Int, ItemExtendFolderAddBinding>(R.layout.item_extend_folder_add) {
                    initView { _, _, _ ->
                        itemBinding.itemLayout.setOnClickListener {
                            if (FastClickFilter.isNeedFilter())
                                return@setOnClickListener
                            showExtendFolderDialog()
                        }
                    }
                }

                addItem<ExtendFolderEntity, ItemExtendFolderBinding>(R.layout.item_extend_folder) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            val fileCountText = "${data.childCount}视频"

                            folderTv.text = getFolderName(data.folderPath)
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

        viewModel.extendAppendedLiveData.observe(this) {
            fileManagerDialog?.dismiss()
        }

        viewModel.getExtendFolder()
    }

    private fun showExtendFolderDialog() {
        fileManagerDialog?.dismiss()
        fileManagerDialog = FileManagerDialog(
            mAttachActivity,
            FileManagerAction.ACTION_SELECT_DIRECTORY,
            dismissWhenClickPositive = false
        ) {
            viewModel.addExtendFolder(it)
        }.also {
            it.show()
        }
    }

    private fun showConfirmRemoveDialog(entity: ExtendFolderEntity) {
        CommonDialog.Builder(requireActivity()).apply {
            content = "确认移除文件夹？"
            addPositive {
                it.dismiss()
                viewModel.removeExtendFolder(entity)
            }
            addNegative { it.dismiss() }
        }.build().show()
    }
}