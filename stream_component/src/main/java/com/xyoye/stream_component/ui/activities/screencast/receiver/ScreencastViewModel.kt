package com.xyoye.stream_component.ui.activities.screencast.receiver

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.stream_component.utils.screencast.receiver.HttpServer
import com.xyoye.stream_component.utils.screencast.receiver.UdpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import kotlin.random.Random

class ScreencastViewModel : BaseViewModel() {
    var serverStatusLiveData = MutableLiveData<Boolean>()

    val displayIp = ObservableField<String>()
    val displayPort = ObservableField<String>()

    var multicastJob: Job? = null
    var httpServer: HttpServer? = null

    var httpPort = 0
    val ipList = mutableListOf<String>()

    fun initIpPort() {
        viewModelScope.launch(Dispatchers.IO) {
            val ipAddresses = mutableListOf<String>()
            showLoading()
            try {
                val element = NetworkInterface.getNetworkInterfaces()
                while (element.hasMoreElements()) {
                    val networkInterface = element.nextElement()
                    val inetAddresses = networkInterface.inetAddresses
                    while (inetAddresses.hasMoreElements()) {
                        val inetAddress = inetAddresses.nextElement()
                        if (inetAddress.isLoopbackAddress || inetAddress.isLinkLocalAddress) {
                            continue
                        }

                        val ipAddress = inetAddress.hostAddress?.toString()
                        if (ipAddress == null || ipAddress.isEmpty()) {
                            continue
                        }
                        if (inetAddress is Inet4Address) {
                            ipAddresses.add(0, ipAddress)
                        } else {
                            ipAddresses.add(ipAddress)
                        }
                    }
                }
            } catch (e: SocketException) {
                e.printStackTrace()
            }
            hideLoading()

            ipList.clear()
            ipList.addAll(ipAddresses)
            displayIp.set(ipAddresses.joinToString(separator = "\n"))

            httpPort = Random.nextInt(20000, 30000)
            displayPort.set(httpPort.toString())
        }
    }

    fun startServer(password: String? = null) {
        stopServer()
        multicastJob = viewModelScope.launch(Dispatchers.IO) {
            showLoading()
            //启动HTTP服务器
            val httpServer = startHttpServer(password, httpPort)
            hideLoading()
            if (httpServer == null) {
                ToastCenter.showError("启动投屏服务器失败")
                return@launch
            }
            this@ScreencastViewModel.httpServer = httpServer

            AppConfig.putLastUsedScreencastPwd(password ?: "")
            AppConfig.putUseScreencastPwd(password.isNullOrEmpty().not())

            displayPort.set(httpPort.toString())
            serverStatusLiveData.postValue(true)
            //启动UDP组播
            UdpServer.startMulticast(
                httpServer.listeningPort,
                needPassword = password.isNullOrEmpty().not()
            )
        }
    }

    fun stopServer() {
        UdpServer.stopMulticast()
        httpServer?.stop()
        multicastJob?.cancel()
        serverStatusLiveData.postValue(false)
    }

    fun createRandomPwd(): String {
        val uuid = UUID.randomUUID().toString()
        return uuid.substring(0, 8)
    }

    private fun startHttpServer(password: String?, port: Int, retry: Boolean = true): HttpServer? {
        httpPort = port
        return try {
            val httpServer = HttpServer(viewModelScope, password, port)
            httpServer.start(2000)
            httpServer
        } catch (e: Exception) {
            e.printStackTrace()
            if (retry) {
                startHttpServer(password, port + 1, false)
            } else {
                null
            }
        }
    }
}