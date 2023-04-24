package com.xyoye.stream_component.ui.activities.storage_plus

import android.app.Dialog
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType

import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityStoragePlusBinding
import com.xyoye.stream_component.ui.dialog.ExternalStorageEditDialog

@Route(path = RouteTable.Stream.StoragePlus)
class StoragePlusActivity : BaseActivity<StoragePlusViewModel, ActivityStoragePlusBinding>() {

    @Autowired
    @JvmField
    var mediaType: MediaType? = null

    @Autowired
    @JvmField
    var editData: MediaLibraryEntity? = null

    private var storageEditDialog: Dialog? = null

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

    private fun checkBundle(): Boolean {
        mediaType ?: return false
        return true
    }

    private fun initObserver() {
        viewModel.exitLiveData.observe(this) {
            finish()
        }
    }

    private fun showDialog() {
        val dialog = when (mediaType) {
            MediaType.EXTERNAL_STORAGE -> createExternalDialog()
            else -> null
        } ?: return

        dialog.show()
        storageEditDialog = dialog
    }

    private fun createExternalDialog(): Dialog {
        return ExternalStorageEditDialog(this, editData) {
            viewModel.addExternalStorage(it)
        }
    }
}