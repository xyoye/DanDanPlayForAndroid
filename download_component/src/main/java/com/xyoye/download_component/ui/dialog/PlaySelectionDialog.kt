package com.xyoye.download_component.ui.dialog

import com.xunlei.downloadlib.XLTaskHelper
import com.xunlei.downloadlib.parameter.TorrentFileInfo
import com.xunlei.downloadlib.parameter.TorrentInfo
import com.xyoye.common_component.adapter.BaseAdapter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.initData
import com.xyoye.common_component.extension.vertical
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

    private lateinit var fileInfoAdapter: BaseAdapter<TorrentFileInfo>

    override fun getChildLayoutId() = R.layout.dialog_play_selection

    override fun initView(binding: DialogPlaySelectionBinding) {

        val torrentInfo = getTorrentInfo(filePath) ?: return

        torrentFileList.addAll(torrentInfo.mSubFileInfo.toMutableList())
        torrentFileList.sortWith { o1, o2 -> o2.mFileSize.compareTo(o1.mFileSize) }
        val defaultChecked = torrentFileList.size == 1
        torrentFileList.forEach { it.checked = defaultChecked }

        setTitle("选择播放文件")

        initRv(binding, torrentFileList)

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
            selectCallback.invoke(checkedFile.mFileIndex)
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
        if (torrentInfo?.mSubFileInfo == null) {
            dismiss()
            mOwnerActivity?.finish()
            ToastCenter.showError("解析种子文件失败")
            return null
        }

        return torrentInfo
    }

    private fun initRv(
        binding: DialogPlaySelectionBinding,
        torrentFileList: MutableList<TorrentFileInfo>
    ) {
        fileInfoAdapter = buildAdapter {
            initData(torrentFileList)

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