package com.xyoye.stream_component.ui.activities.screencast.receiver

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.view.isInvisible
import com.alibaba.android.arouter.facade.annotation.Route
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScan
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.RemoteScanData
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityScreenCastBinding


@Route(path = RouteTable.Stream.ScreencastReceiver)
class ScreencastActivity : BaseActivity<ScreencastViewModel, ActivityScreenCastBinding>() {

    private lateinit var multicastLock: WifiManager.MulticastLock

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            ScreencastViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_screen_cast

    override fun initView() {

        title = "投屏接收端"

        initUdpLock()

        initListener()

        initPassword()

        setupDisableStyle()

        viewModel.initIpPort()
    }

    override fun onDestroy() {
        viewModel.stopServer()
        multicastLock.release()
        super.onDestroy()
    }

    private fun initUdpLock() {
        val manager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        multicastLock = manager.createMulticastLock("udp_multicast")
        multicastLock.acquire()
    }

    private fun initListener() {
        dataBinding.serverSwitchTv.setOnClickListener {
            if (viewModel.serverStatusLiveData.value == true) {
                viewModel.stopServer()
                return@setOnClickListener
            }
            var password: String? = null
            if (dataBinding.setPasswordRb.isChecked) {
                val inputPwd = dataBinding.passwordEt.text.toString()
                if (inputPwd.isEmpty() || inputPwd.length != 8) {
                    ToastCenter.showWarning("密码需要设置为8位")
                    return@setOnClickListener
                }
                password = inputPwd
            }
            viewModel.startServer(password)
        }

        dataBinding.passwordRadioGp.setOnCheckedChangeListener { _, checkedId ->
            val isNoPassword = dataBinding.noPasswordRb.id == checkedId
            dataBinding.passwordEt.isEnabled = isNoPassword.not()
            dataBinding.refreshPasswordIv.isInvisible = isNoPassword

            if (isNoPassword.not()) {
                var password = dataBinding.passwordEt.text.toString()
                if (password.isEmpty()) {
                    password = viewModel.createRandomPwd()
                    dataBinding.passwordEt.setText(password)
                }
            }
        }

        dataBinding.refreshPasswordIv.setOnClickListener {
            val newPassword = viewModel.createRandomPwd()
            dataBinding.passwordEt.setText(newPassword)
        }

        viewModel.serverStatusLiveData.observe(this) {
            if (it) {
                setupEnabledStyle()
            } else {
                setupDisableStyle()
            }
        }
    }

    private fun initPassword() {
        val isUsePwd = AppConfig.isUseScreencastPwd()
        dataBinding.noPasswordRb.isChecked = isUsePwd.not()
        dataBinding.setPasswordRb.isChecked = isUsePwd
        dataBinding.refreshPasswordIv.isInvisible = isUsePwd.not()

        val lastUsedPwd = AppConfig.getLastUsedScreencastPwd()
        if (lastUsedPwd.isNullOrEmpty() && isUsePwd) {
            val randomPwd = viewModel.createRandomPwd()
            dataBinding.passwordEt.setText(randomPwd)
        } else if (lastUsedPwd.isNullOrEmpty().not()) {
            dataBinding.passwordEt.setText(lastUsedPwd)
        }
    }

    private fun setupDisableStyle() {
        createQRCode("请启动投屏接收服务", false)?.let {
            dataBinding.qrCodeIv.setImageBitmap(it)
        }
        dataBinding.serverStatusTv.text = "未启动"
        dataBinding.serverStatusTv.setTextColor(R.color.text_red.toResColor())

        dataBinding.serverSwitchTv.text = "启动服务"
        dataBinding.serverSwitchTv.setTextColor(R.color.text_white.toResColor())
        dataBinding.serverSwitchTv.isSelected = false

        dataBinding.setPasswordRb.isEnabled = true
        dataBinding.noPasswordRb.isEnabled = true
        dataBinding.passwordEt.isEnabled = true
        dataBinding.refreshPasswordIv.isEnabled = true
    }

    private fun setupEnabledStyle() {
        val qrCodeContent = RemoteScanData(
            viewModel.ipList,
            viewModel.httpPort,
            Build.MODEL,
            dataBinding.setPasswordRb.isChecked,
            null
        )
        val qrCodeJson = JsonHelper.toJson(qrCodeContent) ?: ""
        createQRCode(qrCodeJson, true)?.let {
            dataBinding.qrCodeIv.setImageBitmap(it)
        }

        dataBinding.serverStatusTv.text = "已启动"
        dataBinding.serverStatusTv.setTextColor(R.color.text_theme.toResColor())

        dataBinding.serverSwitchTv.text = "停止服务"
        dataBinding.serverSwitchTv.setTextColor(R.color.text_black.toResColor())
        dataBinding.serverSwitchTv.isSelected = true

        dataBinding.setPasswordRb.isEnabled = false
        dataBinding.noPasswordRb.isEnabled = false
        dataBinding.passwordEt.isEnabled = false
        dataBinding.refreshPasswordIv.isEnabled = false
    }

    private fun createQRCode(content: String, enable: Boolean = true): Bitmap? {
        val logoRes = if (enable) R.mipmap.ic_logo else R.mipmap.ic_logo_gray
        val bmpColor = if (enable) R.color.text_black else R.color.text_gray

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