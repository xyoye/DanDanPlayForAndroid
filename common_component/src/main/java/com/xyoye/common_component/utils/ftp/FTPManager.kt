package com.xyoye.common_component.utils.ftp

import com.xyoye.common_component.utils.DDLog
import com.xyoye.common_component.utils.IOUtils
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import java.io.*
import java.net.SocketException

/**
 * Created by xyoye on 2021/1/29.
 */

class FTPManager private constructor() {

    private val ftpClient = FTPClient()
    private var ip: String? = null
    private var port: Int = -1
    private var account: String? = null
    private var password: String? = null
    private var encoding: String? = null
    private var isActive: Boolean = false
    private var isAnonymous: Boolean = false

    private object Holder {
        val instance = FTPManager()
    }

    companion object {
        @JvmStatic
        fun getInstance() = Holder.instance
    }

    fun initConfig(
        ip: String,
        port: Int,
        account: String?,
        password: String?,
        encoding: String,
        isActive: Boolean,
        isAnonymous: Boolean
    ) {
        this.ip = ip
        this.port = port
        this.encoding = encoding
        this.isActive = isActive
        this.isAnonymous = isAnonymous

        if (isAnonymous) {
            this.account = "Anonymous"
            this.password = ""
        } else {
            this.account = account
            this.password = password
        }
    }

    @Throws(FTPException::class)
    fun connect() {
        if (!checkConfig()) {
            DDLog.e("FTP连接参数错误")
            throw IllegalArgumentException("FTP连接参数错误")
        }

        //连接到FTP
        try {
            ftpClient.controlEncoding = encoding
            ftpClient.connect(ip, port)
            val replyCode = ftpClient.replyCode
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                disconnect()
                throw FTPException()
            }

        } catch (e: SocketException) {
            DDLog.e("FTP连接失败", e)
            disconnect()
            throw FTPException(e.message, e)
        } catch (e: IOException) {
            DDLog.e("FTP连接失败", e)
            disconnect()
            throw FTPException(e.message, e)
        }

        //登录
        try {
            if (!ftpClient.login(account, password)) {
                DDLog.w("登录失败")
                disconnect()
                throw FTPException()
            }
        } catch (e: Exception) {
            DDLog.e("FTP登录失败", e)
            disconnect()
            throw FTPException(e.message, e)
        }

        //连接模式
        if (isActive) {
            ftpClient.enterLocalActiveMode()
        } else {
            ftpClient.enterLocalPassiveMode()
        }

        //设置为二进制文件传输
        try {
            if (!ftpClient.setFileType(FTP.BINARY_FILE_TYPE)) {
                disconnect()
                throw FTPException()
            }
        } catch (e: Exception) {
            DDLog.e("无法设置传输模式为二进制传输")
            disconnect()
            throw FTPException(e.message, e)
        }
    }

    @Throws(FTPException::class)
    fun listFiles(dirPath: String): MutableList<FTPFile> {
        val ftpFileList = mutableListOf<FTPFile>()

        var execConnect = false

        //检查连接状态
        if (!ftpClient.isAvailable) {
            connect()
            execConnect = true
        }

        //获取文件列表
        try {
            val fileList = ftpClient.listFiles(dirPath)
            if (fileList != null) {
                DDLog.i("open success, path: $dirPath, count: ${fileList.size}")
                ftpFileList.addAll(fileList)
            }
        } catch (e: Exception) {
            DDLog.e("获取文件列表失败")
            if (execConnect) {
                disconnect()
            }

            throw FTPException(e.message, e)
        }

        if (execConnect) {
            disconnect()
        }

        return ftpFileList
    }

    @Throws(FTPException::class)
    fun getInputStream(dirPath: String, fileName: String): InputStream {
        var inputStream: InputStream? = null

        var execConnect = false
        //检查连接状态
        if (!ftpClient.isAvailable) {
            connect()
            execConnect = true
        }

        //切换工作目录
        try {
            if (!ftpClient.changeWorkingDirectory(dirPath)) {
                DDLog.e("切换工作目录失败：$dirPath")

                if (execConnect) {
                    disconnect()
                }
                throw FTPException()
            }
        } catch (e: Exception) {
            DDLog.e("切换工作目录失败：$dirPath")

            if (execConnect) {
                disconnect()
            }
            throw FTPException(e.message, e)
        }

        //获取文件输入流
        try {
            val fileInputStream = ftpClient.retrieveFileStream(fileName)
            if (fileInputStream != null) {
                DDLog.i("obtained play inputStream, path: $dirPath$fileName")
                inputStream = fileInputStream
            }
        } catch (e: Exception) {
            DDLog.e("获取文件流失败")
            if (execConnect) {
                disconnect()
            }

            throw FTPException(e.message, e)
        }

        if (inputStream == null) {
            DDLog.e("获取文件流失败")
            if (execConnect) {
                disconnect()
            }

            throw FTPException()
        }

        //注意关闭FTP连接及输入流
        return inputStream
    }

    @Throws(FTPException::class)
    fun copyFtpFile(resDirPath: String, resFileName: String, targetFile: File): Boolean{

        //检查连接状态
        if (!ftpClient.isAvailable) {
            connect()
        }

        //切换工作目录
        try {
            if (!ftpClient.changeWorkingDirectory(resDirPath)) {
                DDLog.e("切换工作目录失败：$resDirPath")

                disconnect()
                throw FTPException()
            }
        } catch (e: Exception) {
            DDLog.e("切换工作目录失败：$resDirPath")

            disconnect()
            throw FTPException(e.message, e)
        }

        var copyResult = false
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        //获取文件输入流
        try {
            inputStream = ftpClient.retrieveFileStream(resFileName)
            if (inputStream != null) {
                DDLog.i("obtained download inputStream, path: $resDirPath$resFileName")

                if (targetFile.exists()){
                    targetFile.delete()
                }
                targetFile.createNewFile()

                DDLog.i("copy ftp $resDirPath$resFileName -----> ${targetFile.absolutePath}")

                outputStream = BufferedOutputStream(FileOutputStream(targetFile, false))
                val data = ByteArray(512 * 1024)
                var len: Int
                while (inputStream.read(data).also { len = it } != -1) {
                    outputStream.write(data, 0, len)
                }
                outputStream.flush()
                copyResult = true
            }
        } catch (e: Exception) {
            DDLog.e("获取文件流失败")

            throw FTPException(e.message, e)
        } finally {
            disconnect()
            IOUtils.closeIO(inputStream)
            IOUtils.closeIO(outputStream)
        }

        return copyResult
    }


    @Throws(FTPException::class)
    fun disconnect() {
        if (ftpClient.isAvailable) {
            try {
                ftpClient.logout()
            } catch (e: Exception) {
                e.printStackTrace()
                DDLog.e("退出登录失败", e)
                throw FTPException(e.message, e)
            }

            try {
                ftpClient.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                DDLog.e("断开FTP连接失败", e)
                throw FTPException(e.message, e)
            }
        }
    }

    private fun checkConfig(): Boolean {
        if (ip.isNullOrEmpty())
            return false
        if (port < 0)
            return false
        if (!isAnonymous && (account.isNullOrEmpty() || password.isNullOrEmpty())) {
            return false
        }

        if (encoding.isNullOrEmpty())
            return false

        return true
    }
}