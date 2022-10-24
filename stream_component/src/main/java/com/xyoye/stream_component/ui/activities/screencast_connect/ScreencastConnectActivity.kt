package com.xyoye.stream_component.ui.activities.screencast_connect

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
import com.xyoye.stream_component.databinding.ActivityScreencastConnectBinding
import com.xyoye.stream_component.ui.activities.remote_login.RemoteLoginActivity
import com.xyoye.stream_component.ui.dialog.RemoteLoginDialog
import com.xyoye.stream_component.ui.dialog.ScreencastConnectDialog

@Route(path = RouteTable.Stream.ScreencastConnect)
class ScreencastConnectActivity :
    BaseActivity<ScreencastConnectViewModel, ActivityScreencastConnectBinding>() {
    companion object {
        private const val REQUEST_CODE_REMOTE_SCAN = 1002
    }

    @Autowired
    @JvmField
    var editData: MediaLibraryEntity? = null

    private lateinit var connectDialog: ScreencastConnectDialog

    override fun initViewModel() = ViewModelInit(
        BR.viewModel,
        ScreencastConnectViewModel::class.java
    )

    override fun getLayoutId() = R.layout.activity_screencast_connect

    override fun initView() {
        ARouter.getInstance().inject(this)

        connectDialog = ScreencastConnectDialog(
            this,
            originalStorage = editData,
            devicesLiveData = viewModel.foundDevices,
            onConnectDevice = { device, password -> viewModel.connectDevice(device, password) },
            connectResult = viewModel.connectResult,
            scanQRCode = { launchScanActivity() }
        )

        connectDialog.show()

        viewModel.startReceive()
    }

    override fun onDestroy() {
        viewModel.stopReceive()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_REMOTE_SCAN && resultCode == RESULT_OK) {
            val scanData = data?.getParcelableExtra<RemoteScanData>("scan_data") ?: return
            if (this::connectDialog.isInitialized) {
                connectDialog.setScanResult(scanData)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun launchScanActivity() {
        requestPermissions(Manifest.permission.CAMERA, Manifest.permission.VIBRATE) {
            onGranted {
                ARouter.getInstance()
                    .build(RouteTable.Stream.RemoteScan)
                    .navigation(
                        this@ScreencastConnectActivity,
                        REQUEST_CODE_REMOTE_SCAN
                    )
            }
            onDenied { _, _ ->
                ToastCenter.showError("获取相机权限失败，无法进行扫码")
            }
        }
    }
}