package com.xyoye.common_component.utils.smb

import com.xyoye.data_component.entity.MediaLibraryEntity
import java.io.InputStream

/**
 * Created by xyoye on 2021/2/2.
 */

interface SmbManager {

    fun testConnect(smbData: MediaLibraryEntity): Boolean

    fun initConfig(ip: String, account: String?, password: String?, isAnonymous: Boolean)

    fun connect()

    fun listFiles(smbPath: String): MutableList<SMBFile>

    fun getInputStream(smbPath: String, isDownload: Boolean = false): InputStream

    fun closeStream()

    fun disConnect()
}