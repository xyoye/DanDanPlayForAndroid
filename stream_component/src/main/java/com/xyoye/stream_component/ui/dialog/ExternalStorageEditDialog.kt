package com.xyoye.stream_component.ui.dialog

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.extension.toResDrawable
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.DialogExternalStorageEditBinding
import com.xyoye.stream_component.ui.activities.external_storage.ExternalStorageActivity

/**
 * Created by xyoye on 2023/1/4
 */

class ExternalStorageEditDialog(
    private val activity: ExternalStorageActivity,
    private val storage: MediaLibraryEntity?,
    private val addStorage: (MediaLibraryEntity) -> Unit
) : BaseBottomDialog<DialogExternalStorageEditBinding>(activity) {

    private lateinit var binding: DialogExternalStorageEditBinding
    private var mDocumentFile: DocumentFile? = null

    override fun getChildLayoutId() = R.layout.dialog_external_storage_edit

    override fun initView(binding: DialogExternalStorageEditBinding) {
        this.binding = binding

        if (storage == null) {
            setTitle("添加设备存储库")
        } else {
            setTitle("编辑设备存储库")
            binding.displayNameEt.setText(storage.displayName)
            binding.pathTv.text = storage.describe
            binding.selectRootTv.isEnabled = false
            binding.selectRootTv.setTextColor(R.color.text_gray.toResColor(activity))
            binding.selectRootTv.background =
                R.drawable.background_button_corner_disable.toResDrawable(activity)
        }

        initListener()
    }

    fun setDocumentResult(documentFile: DocumentFile) {
        mDocumentFile = documentFile
        binding.pathTv.text = getDescribe(documentFile)
        if (binding.displayNameEt.text?.toString().isNullOrEmpty()) {
            binding.displayNameEt.setText(getDisplayName(documentFile))
        }
    }

    private fun initListener() {
        binding.selectRootTv.setOnClickListener {
            activity.openDocumentTree()
        }

        setPositiveListener {
            if (storage != null) {
                val newStorage = updateStorage(storage)
                if (newStorage != null) {
                    addStorage.invoke(newStorage)
                }
                dismiss()
                return@setPositiveListener
            }

            val newStorage = createStorage()
                ?: return@setPositiveListener
            addStorage.invoke(newStorage)
            dismiss()
        }

        setNegativeListener {
            dismiss()
        }

        setOnDismissListener {
            activity.finish()
        }
    }

    private fun updateStorage(storage: MediaLibraryEntity): MediaLibraryEntity? {
        val newDisplayName = getDisplayNameByEdit(storage)
        if (newDisplayName == storage.displayName) {
            return null
        }

        storage.displayName = getDisplayNameByEdit(storage)
        return storage
    }

    private fun createStorage(): MediaLibraryEntity? {
        if (mDocumentFile == null) {
            ToastCenter.showError("请选择设备存储库文件夹")
            return null
        }
        val documentFile = mDocumentFile!!

        var displayName = binding.displayNameEt.text?.toString()
        if (displayName.isNullOrEmpty()) {
            displayName = getDisplayName(documentFile)
        }
        return MediaLibraryEntity(
            displayName = displayName,
            url = documentFile.uri.toString(),
            describe = getDescribe(documentFile),
            mediaType = MediaType.EXTERNAL_STORAGE
        )
    }

    private fun getDisplayNameByEdit(storage: MediaLibraryEntity): String {
        val displayName = binding.displayNameEt.text?.toString()
        if (displayName.isNullOrEmpty()) {
            return if (mDocumentFile != null) {
                getDisplayName(mDocumentFile!!)
            } else {
                getDisplayName(storage)
            }
        }
        return displayName
    }

    private fun getDisplayName(storage: MediaLibraryEntity): String {
        val uri = Uri.parse(storage.url)
        val documentFile = DocumentFile.fromTreeUri(activity, uri)
            ?: return uri.lastPathSegment
                ?: return "未知存储库"
        return getDisplayName(documentFile)
    }

    private fun getDisplayName(documentFile: DocumentFile): String {
        val treeId = getTreeDocumentId(documentFile.uri)
        if (treeId == "primary:") {
            return "内部存储设备"
        }
        return documentFile.name ?: documentFile.uri.path ?: documentFile.uri.toString()
    }

    private fun getDescribe(documentFile: DocumentFile): String {
        return documentFile.uri.path ?: documentFile.uri.toString()
    }

    private fun getTreeDocumentId(documentUri: Uri): String? {
        val paths = documentUri.pathSegments
        if (paths.size >= 2 && "tree" == paths[0]) {
            return paths[1]
        }
        return null
    }
}