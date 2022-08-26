package com.xyoye.local_component.ui.dialog

import android.app.Activity
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.data.SubFileData
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogSubtitleFileListBinding
import com.xyoye.local_component.databinding.ItemSubtitleFileBinding

/**
 * Created by xyoye on 2020/12/9.
 */

class SubtitleFileListDialog(
    activity: Activity,
    private val subtitleList: MutableList<SubFileData>,
    private val callback: (fileName: String, url: String) -> Unit
) : BaseBottomDialog<DialogSubtitleFileListBinding>(activity) {

    override fun getChildLayoutId() = R.layout.dialog_subtitle_file_list

    override fun initView(binding: DialogSubtitleFileListBinding) {
        disableSheetDrag()

        setTitle("选择字幕文件下载")

        setNegativeListener { dismiss() }
        setPositiveVisible(false)

        val iterator = subtitleList.iterator()
        while (iterator.hasNext()) {
            val fileData = iterator.next()
            if (fileData.url == null || fileData.f == null)
                iterator.remove()
        }

        binding.subtitleFileRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {

                addItem<SubFileData, ItemSubtitleFileBinding>(R.layout.item_subtitle_file) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            fileNameTv.text = data.f
                            fileSizeTv.text = data.s
                            itemLayout.setOnClickListener {
                                dismiss()
                                callback.invoke(data.f!!, data.url!!)
                            }
                        }
                    }
                }
            }

            setData(subtitleList)
        }
    }
}