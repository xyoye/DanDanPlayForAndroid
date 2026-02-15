package com.xyoye.common_component.storage.impl

import android.net.Uri
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.rapid7.client.dcerpc.mssrvs.ServerService
import com.rapid7.client.dcerpc.transport.SMBTransportFactories
import com.xyoye.common_component.extension.open
import com.xyoye.common_component.extension.openDirectory
import com.xyoye.common_component.extension.openFile
import com.xyoye.common_component.extension.standardFileInfo
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.helper.SmbPlayServer
import com.xyoye.common_component.storage.file.impl.SmbStorageFile
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import java.io.InputStream

/**
 * Created by xyoye on 2023/1/14.
 */

class SmbStorage(library: MediaLibraryEntity) : AbstractStorage(library) {

    private var mSmbConfig = SmbConfig.builder()
        .withDfsEnabled(false)
        .withMultiProtocolNegotiate(true)
        .withBufferSize(16 * 1024 * 1024)
        .build()
    private var mSmbClient = SMBClient()
    private var mSmbSession: Session? = null
    private var mDiskShare: DiskShare? = null

    override var rootUri: Uri = Uri.parse("smb://${library.url}")

    override suspend fun listFiles(file: StorageFile): List<StorageFile> {
        //检测SMB通讯是否正常
        if (checkConnection().not()) {
            return emptyList()
        }
        //要打开的是否为根目录
        if (file.isRootFile()) {
            return getRootShares()
        }
        //只处理SMB文件
        if (file !is SmbStorageFile) {
            return emptyList()
        }
        //切换共享目录
        val switchSuccess = switchShareDisk(file.getShareName()!!, showToast = true)
        if (switchSuccess.not()) {
            return emptyList()
        }
        //展开文件夹
        return listDirectory(file.filePath())
    }

    override suspend fun getRootFile(): StorageFile? {
        //媒体库未预设共享文件夹，则根目录为SMB共享库
        if (library.smbSharePath.isNullOrEmpty()) {
            return SmbStorageFile(this, null, "")
        }
        //检测SMB通讯是否正常
        if (checkConnection().not()) {
            return null
        }
        //切换共享目录
        val shareName = Uri.parse(library.smbSharePath).pathSegments.first()
        val switchSuccess = switchShareDisk(shareName)
        if (switchSuccess.not()) {
            return null
        }
        return SmbStorageFile(this, shareName, "")
    }

    override suspend fun openFile(file: StorageFile): InputStream? {
        //检测SMB通讯是否正常
        if (checkConnection().not()) {
            return null
        }

        val shareName = (file as SmbStorageFile).getShareName()
            ?: return null
        if (switchShareDisk(shareName).not()) {
            return null
        }

        return try {
            mDiskShare?.openFile(file.filePath())?.inputStream
        } catch (e: Exception) {
            e.printStackTrace()

            // 仅关闭 DiskShare 而不是整个连接
            closeDiskShare()

            // 触发重连
            if (switchShareDisk(shareName).not()) {
                return null
            }
            try {
                mDiskShare?.openFile(file.filePath())?.inputStream
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun pathFile(path: String, isDirectory: Boolean): StorageFile? {
        if (checkConnection().not()) {
            return null
        }
        val pathSegments = Uri.parse(path).pathSegments
        val targetShare = pathSegments.firstOrNull()
            ?: return null
        val switchShare = switchShareDisk(targetShare)
        if (switchShare.not()) {
            return null
        }
        val diskShare = mDiskShare ?: return null
        if (diskShare.isConnected.not()) {
            return null
        }
        val shareName = diskShare.smbPath.shareName
        val filePath = pathSegments.takeLast(pathSegments.size - 1).joinToString(separator = "/")
        return try {
            diskShare.open(filePath).use { entry ->
                val fileInfo = entry.standardFileInfo()
                val directory = fileInfo.isDirectory
                val fileLength = if (directory) 0L else fileInfo.endOfFile
                SmbStorageFile(this, shareName, filePath, fileLength, directory)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorToast("获取文件信息失败", e)
            null
        }
    }

    override suspend fun historyFile(history: PlayHistoryEntity): StorageFile? {
        val storagePath = history.storagePath ?: return null
        return pathFile(storagePath, false)?.also {
            it.playHistory = history
        }
    }

    override suspend fun createPlayUrl(file: StorageFile): String? {
        if (file !is SmbStorageFile) {
            return null
        }
        val playServer = SmbPlayServer.getInstance()
        val serverStarted = playServer.startSync()
        if (serverStarted.not()) {
            return null
        }
        return playServer.generatePlayUrl(this, file)
    }

    override suspend fun test(): Boolean {
        if (checkConnection().not()) {
            return false
        }
        val rootFile = getRootFile() ?: return false
        return listFiles(rootFile).isNotEmpty()
    }

    override fun close() {
        closeDiskShare()
        // 彻底关闭时释放整个连接
        SmbPlayServer.getInstance().release()
        IOUtils.closeIO(mSmbClient)
    }

    /**
     * 检查SMB连接是否正常，异常则执行重连
     */
    private fun checkConnection(): Boolean {
        if (mSmbSession?.connection?.isConnected == true) {
            return true
        }

        try {
            mSmbSession?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mSmbSession = null

        try {
            val port = if (library.port == 0) SMBClient.DEFAULT_PORT else library.port
            val connection = mSmbClient.connect(library.url, port)
            val session = connection.authenticate(getAuthenticationContext())

            if (session?.connection?.isConnected == true) {
                mSmbSession = session
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 获取SMB身份信息
     */
    private fun getAuthenticationContext(): AuthenticationContext {
        val password = library.password ?: ""

        return if (library.isAnonymous)
            AuthenticationContext.anonymous()
        else
            AuthenticationContext(library.account, password.toCharArray(), null)
    }

    /**
     * 获取SMB共享目录列表
     */
    private fun getRootShares(): List<SmbStorageFile> {
        return try {
            val transport = SMBTransportFactories.SRVSVC.getTransport(mSmbSession)
            ServerService(transport).shares0.map {
                SmbStorageFile(this, it.netName, "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorToast("获取共享目录列表失败", e)
            emptyList()
        }
    }

    /**
     * 展示文件夹
     */
    private fun listDirectory(filePath: String): List<SmbStorageFile> {
        val diskShare = mDiskShare ?: return emptyList()
        val shareName = diskShare.smbPath.shareName

        return try {
            diskShare.openDirectory(filePath)
                .list()
                .filter { it.fileName != "." && it.fileName != ".." }
                .map { info ->
                    val childPath = generateChildPath(filePath, info.fileName)
                
                    val fileAttributes = info.fileAttributes
                    val isDirectory = (fileAttributes and FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value) != 0L
                    val fileLength = if (isDirectory) 0L else info.endOfFile

                    SmbStorageFile(this, shareName, childPath, fileLength, isDirectory)
                }
                .filter { it.filePath().isNotEmpty() }
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorToast("获取文件列表失败", e)
            emptyList()
        }
    }

    /**
     * 切换共享目录
     */
    private fun switchShareDisk(shareName: String, showToast: Boolean = false): Boolean {
        val currentShare = mDiskShare
        if (currentShare != null && currentShare.isConnected && currentShare.smbPath.shareName == shareName) {
            return true
        }
        //关闭现有共享目录
        closeDiskShare()

        if (connectShare(shareName)) {
            return true
        }

        try {
            mSmbSession?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mSmbSession = null

        if (checkConnection()) {
            if (connectShare(shareName)) {
                return true
            }
        }

        if (showToast) {
            ToastCenter.showError("切换共享目录失败")
        }
        return false
    }

    private fun connectShare(shareName: String): Boolean {
        return try {
            val diskShare = mSmbSession?.connectShare(shareName) as? DiskShare?
            if (diskShare?.isConnected == true) {
                mDiskShare = diskShare
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 关闭共享目录
     */
    private fun closeDiskShare() {
        if (mDiskShare == null) {
            return
        }
        try {
            mDiskShare!!.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateChildPath(parent: String, child: String): String {
        return Uri.parse(parent)
            .buildUpon()
            .appendPath(child)
            .build()
            .path
            ?.removePrefix("/")
            ?: ""
    }

    private fun showErrorToast(message: String, e: Exception) {
        ToastCenter.showError("$message: ${e.message}")
    }
}