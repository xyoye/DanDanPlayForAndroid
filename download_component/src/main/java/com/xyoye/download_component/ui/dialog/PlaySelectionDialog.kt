package com.xyoye.download_component.ui.dialog

import com.xunlei.downloadlib.XLTaskHelper
import com.xunlei.downloadlib.parameter.TorrentFileInfo
import com.xunlei.downloadlib.parameter.TorrentInfo
import com.xyoye.common_component.adapter.BaseAdapter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.FileComparator
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.formatFileSize
import com.xyoye.common_component.utils.isVideoFile
import com.xyoye.common_component.utils.view.ItemDecorationSpace
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.download_component.R
import com.xyoye.download_component.databinding.DialogPlaySelectionBinding
import com.xyoye.download_component.databinding.ItemDownloadSelectionBinding
import java.io.File

class PlaySelectionDialog : BaseBottomDialog<DialogPlaySelectionBinding> {
    private lateinit var filePath: String
    private lateinit var selectCallback: (Int) -> Unit

    constructor() : super()

    constructor(
        filePath: String,
        selectCallback: (Int) -> Unit
    ) : super(true) {
        this.filePath = filePath
        this.selectCallback = selectCallback
    }

    private val torrentFileList = mutableListOf<TorrentFileInfo>()

    private lateinit var fileInfoAdapter: BaseAdapter

    override fun getChildLayoutId() = R.layout.dialog_play_selection

    override fun initView(binding: DialogPlaySelectionBinding) {
        setTitle("选择播放文件")

        initTorrentData()

        initRv(binding)

        setNegativeListener {
            dismiss()
            mOwnerActivity?.finish()
        }

        setPositiveListener {
            val checkedFile = torrentFileList.find { it.checked }
            if (checkedFile == null) {
                ToastCenter.showError("请选择一个播放文件")
                return@setPositiveListener
            }
            if (!isVideoFile(checkedFile.mFileName)) {
                ToastCenter.showError("不支持的视频文件格式")
                return@setPositiveListener
            }
            selectCallback.invoke(torrentFileList.indexOf(checkedFile))
            dismiss()
        }
    }

    private fun getTorrentInfo(filePath: String): TorrentInfo? {
        if (filePath.isEmpty()) {
            dismiss()
            mOwnerActivity?.finish()
            ToastCenter.showError("文件路径为空")
            return null
        }

        val torrentFile = File(filePath)
        if (!torrentFile.exists() || !torrentFile.canRead()) {
            dismiss()
            mOwnerActivity?.finish()
            ToastCenter.showError("文件不存在或无法访问：${filePath}")
            return null
        }

        val torrentInfo = XLTaskHelper.getInstance().getTorrentInfo(filePath)
        if (torrentInfo?.mSubFileInfo.isNullOrEmpty()) {
            dismiss()
            mOwnerActivity?.finish()
            ToastCenter.showError("解析种子文件失败")
            return null
        }

        return torrentInfo
    }

    private fun initRv(binding: DialogPlaySelectionBinding) {
        fileInfoAdapter = buildAdapter {

            addItem<TorrentFileInfo, ItemDownloadSelectionBinding>(R.layout.item_download_selection) {
                initView { data, position, _ ->
                    itemBinding.apply {
                        positionTv.text = (position + 1).toString()
                        downloadCb.isChecked = data.checked
                        downloadCb.isClickable = false
                        downloadNameTv.text = data.mFileName
                        downloadSizeTv.text = formatFileSize(data.mFileSize)
                        itemLayout.setOnClickListener {
                            selectFile(torrentFileList, position)
                            notifyItemChanged(position)
                        }
                    }
                }
            }
        }

        binding.selectionRv.apply {
            layoutManager = vertical()

            adapter = fileInfoAdapter

            addItemDecoration(ItemDecorationSpace(0, dp2px(8)))

            setData(torrentFileList)
        }
    }

    private fun initTorrentData() {
        val torrentInfo = getTorrentInfo(filePath) ?: return
        val torrentInfoFiles = torrentInfo.mSubFileInfo
            .filter {
                isVideoFile(it.mFileName)
            }.sortedWith(FileComparator<TorrentFileInfo>(
                value = { it.mFileName },
                isDirectory = { false }
            ))
        torrentFileList.addAll(torrentInfoFiles)

        if (torrentFileList.isEmpty()) {
            dismiss()
            mOwnerActivity?.finish()
            ToastCenter.showError("当前资源内无可播放的视频文件")
            return
        }

        //仅有一个资源，默认选中后关闭弹窗
        if (torrentFileList.size == 1) {
            selectCallback.invoke(0)
            dismiss()
        } else {
            torrentFileList.forEach { it.checked = false }
        }
    }

    private fun selectFile(torrentFileList: MutableList<TorrentFileInfo>, position: Int) {
        for ((index, fileInfo) in torrentFileList.withIndex()) {
            val newStatus = index == position
            if (fileInfo.checked != newStatus) {
                fileInfo.checked = newStatus
                fileInfoAdapter.notifyItemChanged(index)
            }
        }
    }
}