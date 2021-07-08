package com.xyoye.stream_component.ui.activities.remote_login

import android.Manifest
import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.permission.requestPermissions
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.RemoteScanData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityRemoteLoginBinding
import com.xyoye.stream_component.ui.dialog.RemoteLoginDialog

@Route(path = RouteTable.Stream.RemoteLogin)
class RemoteLoginActivity : BaseActivity<RemoteLoginViewModel, ActivityRemoteLoginBinding>() {
    companion object {
        private const val REQUEST_CODE_REMOTE_SCAN = 1001
    }

    @Autowired
    @JvmField
    var editData: MediaLibraryEntity? = null

    private lateinit var loginDialog: RemoteLoginDialog

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            RemoteLoginViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_remote_login

    override fun initView() {
        ARouter.getInstance().inject(this)

        loginDialog = RemoteLoginDialog(
            editData,
            addMediaStorage = {
                viewModel.addRemoteStorage(editData, it)
            },
            testConnect = {
                viewModel.testConnect(it)
            },
            viewModel.testConnectLiveData,
            scanQRCode = {
                launchScanActivity()
            }
        )
        loginDialog.show(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_REMOTE_SCAN && resultCode == RESULT_OK) {
            val scanData = data?.getParcelableExtra<RemoteScanData>("scan_data") ?: return
            if (this::loginDialog.isInitialized) {
                loginDialog.setScanResult(scanData)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun launchScanActivity() {
        requestPermissions(Manifest.permission.CAMERA, Manifest.permission.VIBRATE) {
            onGranted {
                ARouter.getInstance()
                    .build(RouteTable.Stream.RemoteScan)
                    .navigation(this@RemoteLoginActivity, REQUEST_CODE_REMOTE_SCAN)
            }
            onDenied { _, _ ->
                ToastCenter.showError("获取相机权限失败，无法进行扫码")
            }
        }
    }
}