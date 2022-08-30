package com.xyoye.stream_component.ui.activities.screencast.receiver

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.wifi.WifiManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScan
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.utils.dp2px
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

        initUdpLock()

        try {
            val content = "123dsafsdaf123"

            val logo = BitmapFactory.decodeResource(resources, R.mipmap.ic_logo)
            val options = HmsBuildBitmapOption.Creator()
                .setQRLogoBitmap(logo)
                .setBitmapColor(R.color.text_black.toResColor())
                .create()
            val qrBitmap: Bitmap = ScanUtil.buildBitmap(
                content,
                HmsScan.QRCODE_SCAN_TYPE,
                dp2px(200),
                dp2px(200),
                options
            )
            dataBinding.qrCodeIv.setImageBitmap(qrBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initUdpLock() {
        val manager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        multicastLock = manager.createMulticastLock("udp_multicast")
        multicastLock.acquire()
    }

    override fun onDestroy() {
        viewModel.stopServer()
        multicastLock.release()
        super.onDestroy()
    }
}