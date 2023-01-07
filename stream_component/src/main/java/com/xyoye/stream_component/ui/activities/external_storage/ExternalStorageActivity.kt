package com.xyoye.stream_component.ui.activities.external_storage

import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.utils.activity_result.DocumentTreeLifecycleObserver
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity

import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityExternalStorageBinding
import com.xyoye.stream_component.ui.dialog.ExternalStorageEditDialog

@Route(path = RouteTable.Stream.ExternalStorageEdit)
class ExternalStorageActivity : BaseActivity<ExternalStorageViewModel, ActivityExternalStorageBinding>() {

    @Autowired
    @JvmField
    var editData: MediaLibraryEntity? = null

    private val externalStorageObserver = DocumentTreeLifecycleObserver(
        this,
        onDocumentTreeResult()
    )

    private lateinit var storageEditDialog: ExternalStorageEditDialog

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            ExternalStorageViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_external_storage

    override fun initView() {
        ARouter.getInstance().inject(this)

        storageEditDialog = ExternalStorageEditDialog(this, editData) {
            viewModel.addExternalStorage(it)
        }
        storageEditDialog.show()

        viewModel.exitLiveData.observe(this) {
            finish()
        }
    }

    fun openDocumentTree() {
        externalStorageObserver.openDocumentTree()
    }

    private fun onDocumentTreeResult() = block@{ uri: Uri? ->
        if (uri == null) {
            return@block
        }

        if (takePersistableUriPermission(uri).not()) {
            ToastCenter.showError("获取文件夹访问权限失败")
            return@block
        }

        val documentFile = DocumentFile.fromTreeUri(this, uri)
        if (documentFile == null) {
            ToastCenter.showError("无法访问文件夹")
            return@block
        }
        storageEditDialog.setDocumentResult(documentFile)
    }

    private fun takePersistableUriPermission(uri: Uri): Boolean {
        try {
            val modeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, modeFlags)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}