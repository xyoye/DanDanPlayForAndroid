package com.xyoye.stream_component.ui.activities.smb_login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.rapid7.client.dcerpc.mssrvs.ServerService
import com.rapid7.client.dcerpc.transport.SMBTransportFactories
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.openDirectory
import com.xyoye.data_component.entity.MediaLibraryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SmbLoginViewModel : BaseViewModel() {

    val testConnectLiveData = MutableLiveData<Boolean>()

    fun testConnect(serverData: MediaLibraryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val isSuccess = testSmbConnect(serverData)
            testConnectLiveData.postValue(isSuccess)
        }
    }

    fun addWebDavStorage(originalData: MediaLibraryEntity?, serverData: MediaLibraryEntity) {
        viewModelScope.launch {
            if (originalData != null) {
                DatabaseManager.instance.getMediaLibraryDao()
                    .delete(originalData.url, originalData.mediaType)
            }

            if (serverData.displayName.isEmpty()) {
                serverData.displayName = "SMB媒体库"
            }
            serverData.describe = "smb://${serverData.url}"

            DatabaseManager.instance
                .getMediaLibraryDao()
                .insert(serverData)
        }
    }

    private fun testSmbConnect(library: MediaLibraryEntity) = runBlocking {
        var connected = false
        var smbClient: SMBClient? = null
        var connection: Connection? = null
        var session: Session? = null
        try {
            smbClient = SMBClient()
            connection = smbClient.connect(library.url)
            val authContext = if (library.isAnonymous)
                AuthenticationContext.anonymous()
            else
                AuthenticationContext(library.account, library.password!!.toCharArray(), null)
            session = connection.authenticate(authContext)

            connected = if (library.smbSharePath.isNullOrEmpty()) {
                //无共享路径
                val transport = SMBTransportFactories.SRVSVC.getTransport(session)
                val serverService = ServerService(transport)
                serverService.shares0.size > 0
            } else {
                //有共享路径
                val diskShare = session.connectShare(library.smbSharePath) as DiskShare
                val directory = diskShare.openDirectory("")
                directory.list().size > 0
            }


        } catch (t: Throwable) {
            t.printStackTrace()
        } finally {
            try {
                session?.close()
                connection?.close(true)
                smbClient?.close()
            } catch (ignore: Throwable) {
            }
        }

        return@runBlocking connected
    }
}