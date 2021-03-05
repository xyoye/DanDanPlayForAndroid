package com.xyoye.stream_component.utils.smb.v2

import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.rapid7.client.dcerpc.mssrvs.ServerService
import com.rapid7.client.dcerpc.transport.SMBTransportFactories
import com.xyoye.common_component.utils.DDLog
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.utils.smb.SMBException
import com.xyoye.stream_component.utils.smb.SMBFile
import com.xyoye.stream_component.utils.smb.SmbManager
import java.io.InputStream


/**
 * Created by xyoye on 2021/2/2.
 */

class SMBJManager private constructor() : SmbManager {

    private lateinit var ip: String
    private var account: String? = null
    private var password: String? = null
    private var isAnonymous: Boolean = false

    private var smbClient: SMBClient? = null
    private var connection: Connection? = null
    private var session: Session? = null
    private var diskShare: DiskShare? = null

    private var videoInputStream: InputStream? = null

    private object Holder {
        val instance = SMBJManager()
    }

    companion object {
        @JvmStatic
        fun getInstance() = Holder.instance
    }

    override fun testConnect(smbData: MediaLibraryEntity): Boolean {
        var connected = false
        var smbClient: SMBClient? = null
        var connection: Connection? = null
        var session: Session? = null
        try {
            smbClient = SMBClient()
            connection = smbClient.connect(smbData.url)
            val authContext = if (smbData.isAnonymous)
                AuthenticationContext.anonymous()
            else
                AuthenticationContext(smbData.account, smbData.password!!.toCharArray(), null)
            session = connection.authenticate(authContext)

            connected = if (smbData.smbSharePath.isNullOrEmpty()) {
                //无共享路径
                val transport = SMBTransportFactories.SRVSVC.getTransport(session)
                val serverService = ServerService(transport)
                serverService.shares0.size > 0
            } else {
                //有共享路径
                val diskShare = session.connectShare(smbData.smbSharePath) as DiskShare
                val directory = SMBFileHelper.openDirectory(diskShare, "")
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

        return connected
    }

    override fun initConfig(ip: String, account: String?, password: String?, isAnonymous: Boolean) {
        this.ip = ip
        this.account = account
        this.password = password
        this.isAnonymous = isAnonymous
    }

    @Throws(SMBException::class)
    override fun connect() {
        if (!checkConfig()) {
            DDLog.e("SMB连接参数错误")
            throw IllegalArgumentException("SMB连接参数错误")
        }
        try {
            smbClient = SMBClient()
            connection = smbClient?.connect(ip)
            //用户信息
            val authContext = if (isAnonymous)
                AuthenticationContext.anonymous()
            else
                AuthenticationContext(account, password!!.toCharArray(), null)
            //验证连接
            session = connection?.authenticate(authContext)
        } catch (e: Exception) {
            DDLog.e("SMB服务连接失败")
            disConnect()
            throw SMBException(e.message, e)
        }
    }

    @Throws(SMBException::class)
    override fun listFiles(smbPath: String): MutableList<SMBFile> {
        val smbFileList = mutableListOf<SMBFile>()

        //断开连接则重连
        if (connection?.isConnected == false) {
            connect()
        }

        try {
            val directory = when {
                //当前开根目录
                smbPath.isEmpty() -> {
                    return listRoot()
                }
                //打开共享目录
                isOpenShare(smbPath) -> {
                    val shareName = smbPath.substring(1)
                    diskShare = session?.connectShare(shareName) as DiskShare
                    SMBFileHelper.openDirectory(diskShare!!, "")
                }
                //打开子目录
                else -> {
                    //断开则重连
                    if (diskShare != null && !diskShare!!.isConnected) {
                        val shareName = diskShare!!.smbPath.shareName
                        diskShare = session?.connectShare(shareName) as DiskShare
                    }
                    SMBFileHelper.openDirectory(diskShare!!, smbPath)
                }
            }

            //遍历文件夹内文件
            for (information in directory.list()) {
                //忽略这两个文件夹
                if (information.fileName == "." || information.fileName == "..")
                    continue
                //拼接子文件路径
                val childPath = "$smbPath\\${information.fileName}"
                //获取文件信息
                val isDirectory = SMBFileHelper.isDirectory(diskShare!!, childPath)
                val fileSize = if (isDirectory) 0L else information.endOfFile
                smbFileList.add(
                        SMBFile(information.fileName, fileSize, isDirectory)
                )
            }
        } catch (e: Exception) {
            DDLog.e("获取文件列表失败")
            disConnect()
            throw SMBException(e.message, e)
        }

        return smbFileList
    }

    override fun getInputStream(smbPath: String, isDownload: Boolean): InputStream {
        //断开连接则重连
        if (connection?.isConnected == false) {
            connect()
        }

        try {
            diskShare?.apply {
                //断开则重连
                if (!isConnected) {
                    val shareName = getSmbPath().shareName
                    session?.connectShare(shareName) as DiskShare
                }
            }
            val smbFile = SMBFileHelper.openFile(diskShare!!, smbPath)
            //只记录播放的InputStream
            if (!isDownload) {
                videoInputStream = smbFile.inputStream
            }
        } catch (e: Exception) {
            IOUtils.closeIO(videoInputStream)
            DDLog.e("打开文件失败")
            disConnect()
            throw SMBException(e.message, e)
        }

        return videoInputStream!!
    }

    override fun closeStream() {
        IOUtils.closeIO(videoInputStream)
    }

    @Throws(SMBException::class)
    override fun disConnect() {
        closeStream()

        try {
            session?.close()
        } catch (e: Exception) {
            DDLog.e("退出SMB连接失败", e)
        }
        try {
            connection?.close()
        } catch (e: Exception) {
            DDLog.e("退出SMB连接失败", e)
        }
        try {
            smbClient?.close()
        } catch (e: Exception) {
            DDLog.e("退出SMB连接失败", e)
        }
    }

    /**
     * 获取根目录下共享文件夹
     */
    private fun listRoot(): MutableList<SMBFile> {
        val smbFileList = mutableListOf<SMBFile>()
        val transport = SMBTransportFactories.SRVSVC.getTransport(session)
        val serverService = ServerService(transport)
        serverService.shares0.forEach {
            smbFileList.add(SMBFile(it.netName, 0L, true))
        }
        return smbFileList
    }

    /**
     * 共享目录路径形如：\ShareTest
     */
    private fun isOpenShare(dirName: String): Boolean {
        return dirName.lastIndexOf("\\") == 0
    }

    private fun checkConfig(): Boolean {
        if (!isAnonymous && (account.isNullOrEmpty() || password.isNullOrEmpty())) {
            return false
        }
        return true
    }
}