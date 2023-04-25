package com.xyoye.stream_component.ui.dialog

import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.extension.toResDrawable
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.DialogExternalStorageEditBinding
import com.xyoye.stream_component.ui.activities.storage_plus.StoragePlusActivity
import com.xyoye.stream_component.utils.launcher.DocumentTreeLauncher

/**
 * Created by xyoye on 2023/1/4
 */

class ExternalStorageEditDialog(
    private val activity: StoragePlusActivity,
    private val library: MediaLibraryEntity?,
) : StorageEditDialog<DialogExternalStorageEditBinding>(activity) {

    private lateinit var binding: DialogExternalStorageEditBinding
    private var mDocumentFile: DocumentFile? = null

    private val documentTreeLauncher = DocumentTreeLauncher(activity, onResult())

    override fun getChildLayoutId() = R.layout.dialog_external_storage_edit

    override fun initView(binding: DialogExternalStorageEditBinding) {
        this.binding = binding

        if (library == null) {
            setTitle("添加设备存储库")
        } else {
            setTitle("编辑设备存储库")
            binding.displayNameEt.setText(library.displayName)
            binding.pathTv.text = library.describe
            binding.selectRootTv.isEnabled = false
            binding.selectRootTv.setTextColor(R.color.text_gray.toResColor(activity))
            binding.selectRootTv.background =
                R.drawable.background_button_corner_disable.toResDrawable(activity)
        }

        initListener()
    }

    override fun onTestResult(result: Boolean) {

    }

    private fun setDocumentResult(documentFile: DocumentFile) {
        mDocumentFile = documentFile
        binding.pathTv.text = getDescribe(documentFile)
        if (binding.displayNameEt.text?.toString().isNullOrEmpty()) {
            binding.displayNameEt.setText(getDisplayName(documentFile))
        }
    }

    private fun initListener() {
        binding.selectRootTv.setOnClickListener {
            documentTreeLauncher.launch()
        }

        setPositiveListener {
            if (library != null) {
                val newLibrary = updateLibrary(library)
                if (newLibrary != null) {
                    activity.addStorage(newLibrary)
                }
                return@setPositiveListener
            }

            val newLibrary = createLibrary()
                ?: return@setPositiveListener
            activity.addStorage(newLibrary)
        }

        setNegativeListener {
            activity.finish()
        }
    }

    private fun updateLibrary(library: MediaLibraryEntity): MediaLibraryEntity? {
        val newDisplayName = getDisplayNameByEdit(library)
        if (newDisplayName == library.displayName) {
            return null
        }

        library.displayName = getDisplayNameByEdit(library)
        return library
    }

    private fun createLibrary(): MediaLibraryEntity? {
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

    private fun onResult() = block@{ uri: Uri? ->
        if (uri == null) {
            return@block
        }

        if (takePersistableUriPermission(uri).not()) {
            ToastCenter.showError("获取文件夹访问权限失败")
            return@block
        }

        val documentFile = DocumentFile.fromTreeUri(activity, uri)
        if (documentFile == null) {
            ToastCenter.showError("无法访问文件夹")
            return@block
        }
        setDocumentResult(documentFile)
    }

    private fun takePersistableUriPermission(uri: Uri): Boolean {
        try {
            val modeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            activity.contentResolver.takePersistableUriPermission(uri, modeFlags)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}