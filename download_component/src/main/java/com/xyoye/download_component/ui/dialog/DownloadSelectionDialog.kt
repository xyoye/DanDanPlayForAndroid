package com.xyoye.download_component.ui.dialog

import com.frostwire.jlibtorrent.TorrentInfo
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.initData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.formatFileSize
import com.xyoye.common_component.utils.view.ItemDecorationSpace
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.bean.DownloadSelectionBean
import com.xyoye.download_component.R
import com.xyoye.download_component.databinding.DialogDownloadSelectionBinding
import com.xyoye.download_component.databinding.ItemDownloadSelectionBinding
import java.io.File

/**
 * Created by xyoye on 2021/1/4.
 */

class DownloadSelectionDialog : BaseBottomDialog<DialogDownloadSelectionBinding> {

    private var filePath: String? = null
    private lateinit var selectCallback: (MutableList<Boolean>?) -> Unit

    constructor() : super()

    constructor(
        filePath: String?,
        selectCallback: (MutableList<Boolean>?) -> Unit
    ) : super(true) {
        this.filePath = filePath
        this.selectCallback = selectCallback
    }

    private val fileInfoList = arrayListOf<DownloadSelectionBean>()

    override fun getChildLayoutId() = R.layout.dialog_download_selection

    override fun initView(binding: DialogDownloadSelectionBinding) {
        if (filePath.isNullOrEmpty()) {
            selectCallback.invoke(null)
            mOwnerActivity?.finish()
            ToastCenter.showError("文件路径为空")
            return
        }

        val torrentFile = File(filePath!!)
        if (!torrentFile.exists() || !torrentFile.canRead()) {
            selectCallback.invoke(null)
            mOwnerActivity?.finish()
            ToastCenter.showError("文件不存在或无法访问：${filePath}")
            return
        }

        try {
            val torrentInfo = TorrentInfo(torrentFile)
            showTorrentInfo(torrentInfo, binding)
        } catch (t: Throwable) {
            selectCallback.invoke(null)
            mOwnerActivity?.finish()
            ToastCenter.showError("文件解析失败：${filePath}\n${t.message}")
            return
        }

        setTitle("选择下载文件")

        setNegativeListener {
            dismiss()
            mOwnerActivity?.finish()
        }

        setPositiveListener {
            val selectionList = MutableList(fileInfoList.size) { false }
            for ((index, info) in fileInfoList.withIndex()) {
                if (info.selected) {
                    selectionList[index] = true
                }
            }
            selectCallback.invoke(selectionList)
            mOwnerActivity?.finish()
        }
    }

    private fun showTorrentInfo(torrentInfo: TorrentInfo, binding: DialogDownloadSelectionBinding) {
        val fileStorage = torrentInfo.files()
        for (index in 0 until torrentInfo.numFiles()) {
            val fileName = fileStorage.fileName(index)
            val fileSize = fileStorage.fileSize(index)
            fileInfoList.add(DownloadSelectionBean(fileName, fileSize))
        }

        binding.selectionRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter<DownloadSelectionBean> {
                initData(fileInfoList)

                addItem<DownloadSelectionBean, ItemDownloadSelectionBinding>(R.layout.item_download_selection) {
                    initView { data, position, _ ->
                        itemBinding.apply {
                            positionTv.text = (position + 1).toString()
                            downloadCb.isChecked = data.selected
                            downloadNameTv.text = data.name
                            downloadSizeTv.text = formatFileSize(data.size)
                            downloadCb.setOnCheckedChangeListener { _, isChecked ->
                                data.selected = isChecked
                            }
                            itemLayout.setOnClickListener {
                                data.selected = !data.selected
                                notifyItemChanged(position)
                            }
                        }
                    }
                }
            }

            addItemDecoration(ItemDecorationSpace(0, dp2px(8)))
        }
    }
}