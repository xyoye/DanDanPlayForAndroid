package com.xyoye.storage_component.ui.activities.storage_plus

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.storage_component.BR
import com.xyoye.storage_component.R
import com.xyoye.storage_component.databinding.ActivityStoragePlusBinding
import com.xyoye.storage_component.ui.dialog.AlistStorageEditDialog
import com.xyoye.storage_component.ui.dialog.ExternalStorageEditDialog
import com.xyoye.storage_component.ui.dialog.FTPStorageEditDialog
import com.xyoye.storage_component.ui.dialog.RemoteStorageEditDialog
import com.xyoye.storage_component.ui.dialog.ScreencastStorageEditDialog
import com.xyoye.storage_component.ui.dialog.SmbStorageEditDialog
import com.xyoye.storage_component.ui.dialog.StorageEditDialog
import com.xyoye.storage_component.ui.dialog.WebDavStorageEditDialog

@Route(path = RouteTable.Stream.StoragePlus)
class StoragePlusActivity : BaseActivity<StoragePlusViewModel, ActivityStoragePlusBinding>() {

    @Autowired
    @JvmField
    var mediaType: MediaType? = null

    @Autowired
    @JvmField
    var editData: MediaLibraryEntity? = null

    private var storageEditDialog: StorageEditDialog<*>? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            StoragePlusViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_storage_plus

    override fun initView() {
        ARouter.getInstance().inject(this)

        if (checkBundle().not()) {
            finish()
            return
        }

        initObserver()

        showDialog()
    }

    override fun onDestroy() {
        storageEditDialog?.dismiss()
        super.onDestroy()
    }

    private fun checkBundle(): Boolean {
        mediaType ?: return false
        return true
    }

    private fun initObserver() {
        viewModel.exitLiveData.observe(this) {
            finish()
        }

        viewModel.testLiveData.observe(this) {
            if (storageEditDialog?.isShowing == true) {
                storageEditDialog?.onTestResult(it)
            }
        }
    }

    private fun showDialog() {
        val dialog = when (mediaType) {
            MediaType.EXTERNAL_STORAGE -> ExternalStorageEditDialog(this, editData)
            MediaType.REMOTE_STORAGE -> RemoteStorageEditDialog(this, editData)
            MediaType.FTP_SERVER -> FTPStorageEditDialog(this, editData)
            MediaType.WEBDAV_SERVER -> WebDavStorageEditDialog(this, editData)
            MediaType.SMB_SERVER -> SmbStorageEditDialog(this, editData)
            MediaType.SCREEN_CAST -> ScreencastStorageEditDialog(this, editData)
            MediaType.ALSIT_STORAGE -> AlistStorageEditDialog(this, editData)
            else -> {
                finish()
                null
            }
        } ?: return

        dialog.show()
        storageEditDialog = dialog
    }

    fun addStorage(library: MediaLibraryEntity) {
        viewModel.addStorage(editData, library)
    }

    fun testStorage(library: MediaLibraryEntity) {
        viewModel.testStorage(library)
    }
}