package com.xyoye.stream_component.ui.activities.screencast.receiver

import android.content.Context
import android.net.wifi.WifiManager
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable

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

        dataBinding.tvStartServer.setOnClickListener {
            dataBinding.tvStartServer.isVisible = false
            dataBinding.tvStopServer.isVisible = true
            viewModel.startServer(password = "test")
        }

        dataBinding.tvStopServer.setOnClickListener {
            dataBinding.tvStartServer.isVisible = true
            dataBinding.tvStopServer.isVisible = false
            viewModel.stopServer()
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