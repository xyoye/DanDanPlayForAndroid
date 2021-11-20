//package com.xyoye.common_component.utils.smb
//
//import com.xyoye.common_component.utils.DDLog
//import com.xyoye.data_component.entity.MediaLibraryEntity
//import jcifs.CIFSContext
//import jcifs.context.SingletonContext
//import jcifs.smb.NtlmPasswordAuthenticator
//import jcifs.smb.SmbException
//import jcifs.smb.SmbFile
//import java.util.*
//
///**
// * Created by xyoye on 2021/2/2.
// */
//
//class JCIFSManager private constructor() : SmbManager {
//
//    init {
//        val properties = Properties().apply {
//            setProperty("jcifs.smb.client.enableSMB2", "false")
//            setProperty("jcifs.smb.client.disableSMB1", "false")
//            setProperty("jcifs.smb.client.maxVersion", "SMB1")
//            setProperty("jcifs.smb.client.useLargeReadWrite", "false")
//            setProperty("jcifs.smb.client.responseTimeout", "5000")
//        }
//        SingletonContext.init(properties)
//    }
//
//    private object Holder {
//        val instance = JCIFSManager()
//    }
//
//    companion object {
//        @JvmStatic
//        fun getInstance() = Holder.instance
//
//    }
//
//    override fun testConnect(smbData: MediaLibraryEntity): Boolean {
//        var cifsContext: CIFSContext? = null
//        try {
//            val url = "smb://${smbData.url}"
//
//            val authenticator = NtlmPasswordAuthenticator(smbData.account, smbData.password)
//
//            cifsContext = SingletonContext.getInstance()
//            cifsContext.withCredentials(authenticator)
//
//            val smbFile = SmbFile(url, cifsContext)
//            smbFile.connect()
//
//            //一直都是Access is denied.
//            smbFile.listFiles().forEach {
//                DDLog.e("child name: ${it.name}  isDir:${it.isDirectory}")
//            }
//
//            return true
//        } catch (e: SmbException) {
//            DDLog.e("${e.ntStatus}: ${e.message}")
//            e.printStackTrace()
//        } finally {
//            cifsContext?.close()
//        }
//        return false
//    }
//}