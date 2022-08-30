package com.xyoye.stream_component.ui.activities.screencast_connect

import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.RequestError
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.EntropyUtils
import com.xyoye.data_component.bean.UDPDeviceBean
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.utils.screencast.provider.UdpClient
import com.xyoye.stream_component.utils.screencast.receiver.UdpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreencastConnectViewModel : BaseViewModel() {

    val foundDevices = MutableLiveData<List<UDPDeviceBean>>()
    val connectResult = MutableLiveData<Boolean>()

    private val devices = mutableListOf<UDPDeviceBean>()

    fun startReceive() {
        viewModelScope.launch(Dispatchers.IO) {
            UdpClient.startReceive { considerAddDevice(it) }
        }
    }

    fun stopReceive() {
        UdpClient.release()
    }

    fun connectDevice(device: UDPDeviceBean, password: String?) {
        httpRequest<CommonJsonData>(viewModelScope) {

            onStart { showLoading() }

            api {
                Retrofit.screencastService.init(
                    host = device.ipAddress ?: "",
                    port = device.httpPort,
                    authorization = createAuthorization(password)
                )
            }

            onSuccess {
                if (it.success) {
                    insertMediaLibrary(device, password)
                    connectResult.postValue(true)
                } else {
                    showNetworkError(RequestError(it.errorCode, it.errorMessage ?: "未知错误"))
                }
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }
    }

    private fun considerAddDevice(device: UDPDeviceBean) {
        if (device.ipAddress.isNullOrEmpty() || device.httpPort == 0) {
            return
        }
        val addedDevice = devices.find {
            it.ipAddress == device.ipAddress
        }
        if (addedDevice != null) {
            devices.remove(addedDevice)
        }

        devices.add(device)
        foundDevices.postValue(devices)
    }

    private fun insertMediaLibrary(device: UDPDeviceBean, password: String?) {
        viewModelScope.launch {
            val entity = MediaLibraryEntity(
                displayName = device.deviceName,
                url = device.ipAddress ?: "",
                port = device.httpPort,
                describe = "http://${device.ipAddress}:${device.httpPort}",
                password = password,
                mediaType = MediaType.SCREEN_CAST
            )
            DatabaseManager.instance
                .getMediaLibraryDao()
                .insert(entity)
        }
    }

    private fun createAuthorization(password: String?): String? {
        if (password == null || password.isEmpty()) {
            return null
        }

        val authorization = EntropyUtils.aesEncode(
            UdpServer.multicastMsgKey,
            password,
            Base64.NO_WRAP
        )
        if (authorization == null || authorization.isEmpty()) {
            return null
        }
        return "Bearer $authorization"
    }
}