package com.xyoye.stream_component.ui.activities.document_tree

import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.utils.activity_result.DocumentTreeLifecycleObserver
import com.xyoye.common_component.weight.ToastCenter

import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityDocumentTreeBinding

@Route(path = RouteTable.Stream.DocumentTree)
class DocumentTreeActivity : BaseActivity<DocumentTreeViewModel, ActivityDocumentTreeBinding>() {

    private val externalStorageObserver = DocumentTreeLifecycleObserver(
        this,
        onDocumentTreeResult()
    )

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            DocumentTreeViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_document_tree

    override fun initView() {
        viewModel.exitLiveData.observe(this) {
            finish()
        }

        dataBinding.root.post {
            externalStorageObserver.openDocumentTree()
        }
    }

    private fun onDocumentTreeResult() = block@{ uri: Uri? ->
        if (uri == null) {
            finish()
            return@block
        }

        if (takePersistableUriPermission(uri).not()) {
            ToastCenter.showError("获取文件夹访问权限失败")
            finish()
            return@block
        }

        val documentFile = DocumentFile.fromTreeUri(this, uri)
        if (documentFile == null) {
            ToastCenter.showError("无法访问文件夹")
            finish()
            return@block
        }
        viewModel.addExternalStorage(documentFile)
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