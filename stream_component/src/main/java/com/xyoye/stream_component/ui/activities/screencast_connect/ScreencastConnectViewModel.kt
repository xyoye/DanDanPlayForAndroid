package com.xyoye.stream_component.ui.activities.screencast_connect

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.RequestError
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.data_component.bean.UDPDeviceBean
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.utils.screencast.provider.UdpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreencastConnectViewModel : BaseViewModel() {

    val foundDevices = MutableLiveData<List<UDPDeviceBean>>()
    val connectResult = MutableLiveData<Boolean>()

    private val devices = mutableListOf<UDPDeviceBean>()

    fun startReceive() {
        viewModelScope.launch(Dispatchers.IO) {
            UdpClient.startMulticastReceive { considerAddDevice(it) }
        }
    }

    fun stopReceive() {
        UdpClient.stopMulticastReceive()
    }

    fun connectDevice(device: UDPDeviceBean, password: String?) {
        httpRequest<CommonJsonData>(viewModelScope) {

            onStart { showLoading() }

            api {
                Retrofit.screencastService.init(
                    host = device.ipAddress ?: "",
                    port = device.httpPort,
                    authorization = password
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
            it.ipAddress == device.ipAddress && it.httpPort == device.httpPort
        }
        if (addedDevice != null) {
            devices.remove(addedDevice)
        }

        devices.add(device)
        devices.sortBy { it.ipAddress + it.httpPort }
        foundDevices.postValue(devices)
    }

    private fun insertMediaLibrary(device: UDPDeviceBean, password: String?) {
        viewModelScope.launch {
            val entity = MediaLibraryEntity(
                displayName = device.deviceName,
                url = "http://${device.ipAddress}:${device.httpPort}",
                screencastAddress = device.ipAddress ?: "",
                port = device.httpPort,
                password = password,
                mediaType = MediaType.SCREEN_CAST
            )
            DatabaseManager.instance
                .getMediaLibraryDao()
                .insert(entity)
        }
    }
}