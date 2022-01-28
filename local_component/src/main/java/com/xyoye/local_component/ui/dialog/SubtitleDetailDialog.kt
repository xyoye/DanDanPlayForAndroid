package com.xyoye.local_component.ui.dialog

import androidx.core.view.isVisible
import com.xyoye.common_component.utils.formatFileSize
import com.xyoye.common_component.utils.getFileExtension
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.common_component.utils.seven_zip.SevenZipUtils
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.data.SubDetailData
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.DialogSubtitleDetailBinding

/**
 * Created by xyoye on 2020/12/9.
 */

class SubtitleDetailDialog : BaseBottomDialog<DialogSubtitleDetailBinding> {

    private lateinit var subDetailData: SubDetailData
    private lateinit var downloadOne: () -> Unit
    private lateinit var downloadZip: (fileName: String, url: String) -> Unit

    constructor() : super()
    constructor(
        subDetailData: SubDetailData,
        downloadOne: () -> Unit,
        downloadZip: (fileName: String, url: String) -> Unit
    ) : super(true) {
        this.subDetailData = subDetailData
        this.downloadOne = downloadOne
        this.downloadZip = downloadZip
    }

    private var extension: String? = null

    override fun getChildLayoutId() = R.layout.dialog_subtitle_detail

    override fun initView(binding: DialogSubtitleDetailBinding) {
        setTitle("下载字幕")

        val fileName = subDetailData.filename ?: ""
        val fileNameText = getFileNameNoExtension(fileName)
        binding.fileNameEt.setText(fileNameText)
        binding.fileNameEt.setSelection(fileNameText.length)

        if (!subDetailData.url.isNullOrEmpty()) {
            val fileExtension = getFileExtension(fileName)
            if (SevenZipUtils.getArchiveFormat(fileExtension) != null) {
                extension = ".$fileExtension"
                binding.fileExtensionTv.text = extension
            }
        }

        if (extension == null) {
            setPositiveVisible(false)
            binding.fileNameTips.isVisible = false
            binding.fileNameEt.isVisible = false
            binding.fileExtensionTv.isVisible = false
        }

        val fileSizeText = "文件大小：${formatFileSize(subDetailData.size ?: 0)}"
        binding.fileSizeTv.text = fileSizeText
        val fileCountText = "文件数量：${(subDetailData.filelist?.size ?: 0)}"
        binding.fileCountTv.text = fileCountText
        val subtitleLanguageText = "字幕语种：${subDetailData.lang?.desc}"
        binding.subtitleLanguageTv.text = subtitleLanguageText

        var uploadTime = subDetailData.upload_time ?: ""
        if (uploadTime.contains(" ".toRegex())) {
            uploadTime = uploadTime.split(" ".toRegex())[0]
        }
        val uploadTimeText = "上传时间：$uploadTime"
        binding.subtitleTimeTv.text = uploadTimeText

        setNegativeListener { dismiss() }
        setPositiveText("下载压缩包")

        if (subDetailData.filelist != null && subDetailData.filelist?.size ?: 0 > 0) {
            addNeutralButton("下载单个文件") {
                dismiss()
                downloadOne.invoke()
            }
        }

        setPositiveListener {
            dismiss()
            val zipFileName = binding.fileNameEt.text.toString() + extension
            downloadZip.invoke(zipFileName, subDetailData.url!!)
        }
    }
}