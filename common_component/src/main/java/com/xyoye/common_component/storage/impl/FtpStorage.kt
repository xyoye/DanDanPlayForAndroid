package com.xyoye.common_component.storage.impl

import android.net.Uri
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.helper.FtpPlayServer
import com.xyoye.common_component.storage.file.impl.FtpStorageFile
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.MediaLibraryEntity
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream

/**
 * Created by XYJ on 2023/1/16.
 */

class FtpStorage(library: MediaLibraryEntity) : AbstractStorage(library) {
    private val mFtpClient = FTPClient()
    private var currentWorkDirectory: String? = null

    override suspend fun listFiles(file: StorageFile): List<StorageFile> {
        //检测FTP通讯是否正常
        if (checkConnection().not()) {
            return emptyList()
        }
        //获取文件列表
        try {
            if (file.isRootFile().not() && file is FtpStorageFile) {
                if (currentWorkDirectory != file.parentPath) {
                    mFtpClient.changeWorkingDirectory(file.parentPath)
                    currentWorkDirectory = file.parentPath
                }
            }
            return mFtpClient.listFiles(file.filePath()).map {
                FtpStorageFile(this, file.filePath(), it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorToast("获取文件列表失败", e)
            close()
        }
        return emptyList()
    }

    override suspend fun getRootFile(): StorageFile {
        val ftpFile = FTPFile().apply {
            name = ""
            type = FTPFile.DIRECTORY_TYPE
        }
        return FtpStorageFile(this, "/", ftpFile)
    }

    override suspend fun openFile(file: StorageFile): InputStream? {
        if (file !is FtpStorageFile) {
            return null
        }
        if (checkConnection().not()) {
            return null
        }
        try {
            if (currentWorkDirectory != file.parentPath) {
                mFtpClient.changeWorkingDirectory(file.parentPath)
                currentWorkDirectory = file.parentPath
            }
            return mFtpClient.retrieveFileStream(file.fileName())
        } catch (e: Exception) {
            e.printStackTrace()
            close()
        }
        return null
    }

    suspend fun openFile(file: StorageFile, offset: Long): InputStream? {
        if (offset > 0) {
            mFtpClient.restartOffset = offset
        }
        return openFile(file)
    }

    override suspend fun pathFile(path: String): StorageFile {
        val pathUri = Uri.parse(path)
        val fileName = pathUri.lastPathSegment
        val parentPath = pathUri.path?.removeSuffix("/$fileName") ?: "/"
        val ftpFile = FTPFile().apply {
            name = fileName
            type = FTPFile.DIRECTORY_TYPE
        }
        return FtpStorageFile(this, parentPath, ftpFile)
    }

    override suspend fun createPlayUrl(file: StorageFile): String? {
        if (file !is FtpStorageFile) {
            return null
        }
        val playServer = FtpPlayServer.getInstance()
        val serverStarted = playServer.startSync()
        if (serverStarted.not()) {
            return null
        }
        return playServer.generatePlayUrl(this, file)
    }

    private fun checkConnection(): Boolean {
        if (mFtpClient.isAvailable) {
            return true
        }

        try {
            mFtpClient.controlEncoding = library.ftpEncoding
            mFtpClient.connect(library.ftpAddress, library.port)
            if (checkLogin().not()) {
                return false
            }
            if (library.isActiveFTP) {
                mFtpClient.enterLocalActiveMode()
            } else {
                mFtpClient.enterLocalPassiveMode()
            }
            mFtpClient.setFileType(FTP.BINARY_FILE_TYPE)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorToast("连接至FTP服务失败", e)
            close()
        }
        return false
    }

    private fun checkLogin(): Boolean {
        try {
            return mFtpClient.login(library.account, library.password)
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorToast("登录FTP服务失败", e)
            close()
        }
        return false
    }


    override fun close() {
        try {
            mFtpClient.logout()
            mFtpClient.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

     fun completePending() {
        if (FtpPlayServer.getInstance().isAlive.not()){
            return
        }
        if(!mFtpClient.completePendingCommand()) {
            close()
        }
    }

    private fun showErrorToast(message: String, e: Exception? = null) {
        ToastCenter.showError("$message: ${e?.message}")
    }
}