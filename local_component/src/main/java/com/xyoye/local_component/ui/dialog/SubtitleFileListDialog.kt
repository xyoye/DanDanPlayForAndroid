package com.xyoye.local_component.ui.dialog

import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.initData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.data.SubFileData
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogSubtitleFileListBinding
import com.xyoye.local_component.databinding.ItemSubtitleFileBinding

/**
 * Created by xyoye on 2020/12/9.
 */

class SubtitleFileListDialog : BaseBottomDialog<DialogSubtitleFileListBinding> {
    private lateinit var subtitleList: MutableList<SubFileData>
    private lateinit var callback: (fileName: String, url: String) -> Unit

    constructor() : super()

    constructor(
        subtitleList: MutableList<SubFileData>,
        callback: (fileName: String, url: String) -> Unit
    ) : super(true) {
        this.subtitleList = subtitleList
        this.callback = callback
    }

    override fun getChildLayoutId() = R.layout.dialog_subtitle_file_list

    override fun initView(binding: DialogSubtitleFileListBinding) {
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
                initData(subtitleList)

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
        }
    }
}