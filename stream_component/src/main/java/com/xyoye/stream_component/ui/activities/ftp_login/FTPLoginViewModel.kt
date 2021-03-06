package com.xyoye.stream_component.ui.activities.ftp_login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply

class FTPLoginViewModel : BaseViewModel() {
    val testConnectLiveData = MutableLiveData<Boolean>()

    fun testConnect(serverData: MediaLibraryEntity) {
        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            val ftpClient = FTPClient()
            try {
                ftpClient.apply {
                    //编码格式
                    controlEncoding = serverData.ftpEncoding
                    //连接模式
                    if (serverData.isActiveFTP) {
                        enterLocalActiveMode()
                    } else {
                        enterLocalPassiveMode()
                    }
                    //连接
                    connect(serverData.ftpAddress, serverData.port)
                    //登录
                    if (serverData.isAnonymous) {
                        login("Anonymous", "")
                    } else {
                        login(serverData.account, serverData.password)
                    }
                }
                //状态码判断
                val replyCode = ftpClient.replyCode
                if (FTPReply.isPositiveCompletion(replyCode)) {
                    testConnectLiveData.postValue(true)
                    ToastCenter.showSuccess("连接成功")
                } else {
                    testConnectLiveData.postValue(false)
                    ToastCenter.showSuccess("连接登录失败：x$replyCode")
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                val errorMsg = "连接失败：${t.message}"
                ToastCenter.showWarning(errorMsg)
                testConnectLiveData.postValue(false)
            }

            try {
                ftpClient.abort()
                ftpClient.disconnect()
            } catch (ignore: Throwable) {

            }

            withContext(Dispatchers.Main) {
                hideLoading()
            }
        }
    }

    fun addFTPStorage(originalData: MediaLibraryEntity?, serverData: MediaLibraryEntity) {
        viewModelScope.launch {
            if (originalData != null) {
                DatabaseManager.instance.getMediaLibraryDao()
                    .delete(originalData.url, originalData.mediaType)
            }

            if (serverData.displayName.isEmpty()) {
                serverData.displayName = "FTP媒体库"
            }
            serverData.url = if (serverData.ftpAddress.contains("//"))
                "${serverData.ftpAddress}:${serverData.port}"
            else
                "ftp://${serverData.ftpAddress}:${serverData.port}"
            serverData.describe = serverData.url

            DatabaseManager.instance
                .getMediaLibraryDao()
                .insert(serverData)
        }
    }
}