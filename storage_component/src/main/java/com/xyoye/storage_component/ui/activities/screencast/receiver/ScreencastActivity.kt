package com.xyoye.storage_component.ui.activities.screencast.receiver

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.view.isVisible
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScan
import com.therouter.router.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.bridge.ServiceLifecycleBridge
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.config.ScreencastConfig
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.storage.helper.ScreencastConstants
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.data.RemoteScanData
import com.xyoye.storage_component.BR
import com.xyoye.storage_component.R
import com.xyoye.storage_component.databinding.ActivityScreenCastBinding
import com.xyoye.storage_component.services.ScreencastReceiveService
import kotlin.random.Random


@Route(path = RouteTable.Stream.ScreencastReceiver)
class ScreencastActivity : BaseActivity<ScreencastViewModel, ActivityScreenCastBinding>() {

    private var httpPort = 0

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            ScreencastViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_screen_cast

    override fun initView() {

        title = "投屏接收端"

        initVersion()

        initListener()

        initPort()

        initPassword()

        if (ScreencastReceiveService.isRunning(this)) {
            setupEnabledStyle()
        } else {
            setupDisableStyle()
        }

        dataBinding.needConfirmSwitch.isChecked = ScreencastConfig.getReceiveNeedConfirm()

        dataBinding.autoStartSwitch.isChecked = ScreencastConfig.getStartReceiveWhenLaunch()

        viewModel.initIpPort()
    }

    private fun initListener() {
        ServiceLifecycleBridge.getScreencastReceiveObserver().observe(this) {
            if (it) {
                setupEnabledStyle()
            } else {
                setupDisableStyle()
            }
        }

        dataBinding.serverSwitchTv.setOnClickListener {
            if (ScreencastReceiveService.isRunning(this)) {
                stopService()
                return@setOnClickListener
            }

            startService()
        }

        dataBinding.passwordSwitch.setOnCheckedChangeListener { _, isChecked ->
            dataBinding.passwordEt.isEnabled = isChecked
            dataBinding.refreshPasswordIv.isVisible = isChecked

            if (isChecked) {
                var password = dataBinding.passwordEt.text.toString()
                if (password.isEmpty()) {
                    password = viewModel.createRandomPwd()
                    dataBinding.passwordEt.setText(password)
                }
            } else {
                dataBinding.passwordEt.setText("")
            }
        }

        dataBinding.refreshPasswordIv.setOnClickListener {
            val newPassword = viewModel.createRandomPwd()
            dataBinding.passwordEt.setText(newPassword)
        }

        dataBinding.refreshPortIv.setOnClickListener {
            CommonDialog.Builder(this).run {
                title = "提示"
                content = "确认更换端口号？\n\n更换后已连接设备需要重新连接"
                addPositive {
                    httpPort = Random.nextInt(20000, 30000)
                    ScreencastConfig.setReceiverPort(httpPort)
                    dataBinding.portTv.text = httpPort.toString()
                    it.dismiss()
                }
                addNegative { it.dismiss() }
                build()
            }.show()
        }

        dataBinding.needConfirmSwitch.setOnCheckedChangeListener { _, isChecked ->
            ScreencastConfig.setReceiveNeedConfirm(isChecked)
        }

        dataBinding.autoStartSwitch.setOnCheckedChangeListener { _, isChecked ->
            ScreencastConfig.setStartReceiveWhenLaunch(isChecked)
        }
    }

    private fun initVersion() {
        val versionDisplay = "v${ScreencastConstants.version}"
        dataBinding.versionTv.text = versionDisplay
    }

    private fun initPort() {
        httpPort = ScreencastConfig.getReceiverPort()
        if (httpPort == 0) {
            httpPort = Random.nextInt(20000, 30000)
            ScreencastConfig.setReceiverPort(httpPort)
        }
        dataBinding.portTv.text = httpPort.toString()
    }

    private fun initPassword() {
        val isUsePwd = ScreencastConfig.getUseReceiverPassword()
        dataBinding.passwordSwitch.isChecked = isUsePwd

        val receiverPwd = ScreencastConfig.getReceiverPassword()
        if (receiverPwd.isNullOrEmpty() && isUsePwd) {
            val randomPwd = viewModel.createRandomPwd()
            dataBinding.passwordEt.setText(randomPwd)
        } else if (receiverPwd.isNullOrEmpty().not()) {
            dataBinding.passwordEt.setText(receiverPwd)
        }
    }

    private fun startService() {
        var password: String? = null
        if (dataBinding.passwordSwitch.isChecked) {
            val inputPwd = dataBinding.passwordEt.text.toString()
            if (inputPwd.isEmpty() || inputPwd.length != 8) {
                ToastCenter.showWarning("密码需要设置为8位")
                return
            }
            password = inputPwd
        }

        ScreencastConfig.setReceiverPassword(password ?: "")
        ScreencastConfig.setUseReceiverPassword(password.isNullOrEmpty().not())
        ScreencastReceiveService.start(this, httpPort, password)
    }

    private fun stopService() {
        ScreencastReceiveService.stop(this)
    }

    private fun setupDisableStyle() {
        createQRCode("请启动投屏接收服务", false)?.let {
            dataBinding.qrCodeIv.setImageBitmap(it)
        }
        dataBinding.serverStatusTv.text = "未启动"
        dataBinding.serverStatusTv.setTextColor(com.xyoye.common_component.R.color.text_red.toResColor())

        dataBinding.serverSwitchTv.text = "启动服务"
        dataBinding.serverSwitchTv.setTextColor(com.xyoye.common_component.R.color.text_white.toResColor())
        dataBinding.serverSwitchTv.isSelected = false

        dataBinding.passwordSwitch.isEnabled = true
        dataBinding.passwordEt.isEnabled = dataBinding.passwordSwitch.isChecked
        dataBinding.refreshPasswordIv.isVisible = dataBinding.passwordSwitch.isChecked
        dataBinding.refreshPasswordIv.isEnabled = true
        dataBinding.refreshPortIv.isVisible = true
    }

    private fun setupEnabledStyle() {
        val qrCodeContent = RemoteScanData(
            viewModel.ipList,
            httpPort,
            Build.MODEL,
            dataBinding.passwordSwitch.isChecked,
            null
        )
        val qrCodeJson = JsonHelper.toJson(qrCodeContent) ?: ""
        createQRCode(qrCodeJson, true)?.let {
            dataBinding.qrCodeIv.setImageBitmap(it)
        }

        dataBinding.serverStatusTv.text = "已启动"
        dataBinding.serverStatusTv.setTextColor(com.xyoye.common_component.R.color.text_theme.toResColor())

        dataBinding.serverSwitchTv.text = "停止服务"
        dataBinding.serverSwitchTv.setTextColor(com.xyoye.common_component.R.color.text_black.toResColor())
        dataBinding.serverSwitchTv.isSelected = true

        dataBinding.passwordSwitch.isEnabled = false
        dataBinding.passwordEt.isEnabled = false
        dataBinding.refreshPasswordIv.isVisible = false
        dataBinding.refreshPasswordIv.isEnabled = false
        dataBinding.refreshPortIv.isVisible = false
    }

    private fun createQRCode(content: String, enable: Boolean = true): Bitmap? {
        val logoRes = if (enable) R.mipmap.ic_logo else R.mipmap.ic_logo_gray
        val bmpColor = if (enable) com.xyoye.common_component.R.color.text_black else com.xyoye.common_component.R.color.text_gray

        try {
            val logo = BitmapFactory.decodeResource(resources, logoRes)
            val options = HmsBuildBitmapOption.Creator()
                .setQRLogoBitmap(logo)
                .setBitmapColor(bmpColor.toResColor())
                .create()
            return ScanUtil.buildBitmap(
                content,
                HmsScan.QRCODE_SCAN_TYPE,
                dp2px(200),
                dp2px(200),
                options
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}