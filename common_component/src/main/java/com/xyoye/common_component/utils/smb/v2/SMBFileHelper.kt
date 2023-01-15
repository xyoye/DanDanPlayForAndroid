package com.xyoye.common_component.utils.smb.v2

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.msfscc.fileinformation.FileStandardInformation
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.DiskEntry
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import java.util.*


/**
 * Created by xyoye on 2021/2/3.
 */

object SMBFileHelper {

    fun openDirectory(share: DiskShare, filePath: String): Directory {
        return share.openDirectory(
            filePath,
            EnumSet.of(AccessMask.FILE_LIST_DIRECTORY),
            setOf(FileAttributes.FILE_ATTRIBUTE_DIRECTORY),
            setOf(SMB2ShareAccess.FILE_SHARE_READ),
            SMB2CreateDisposition.FILE_OPEN,
            null
        )
    }

    fun openFile(share: DiskShare, filePath: String): File {
        return share.openFile(
            filePath,
            setOf(AccessMask.FILE_READ_DATA),
            setOf(FileAttributes.FILE_ATTRIBUTE_READONLY),
            setOf(SMB2ShareAccess.FILE_SHARE_READ),
            SMB2CreateDisposition.FILE_OPEN,
            setOf(SMB2CreateOptions.FILE_RANDOM_ACCESS)
        )
    }

    fun isDirectory(diskShare: DiskShare, filePath: String): Boolean {
        val diskEntry = openDiskEntry(diskShare, filePath)
        val standardInformation =
            diskEntry!!.getFileInformation(FileStandardInformation::class.java)
        return standardInformation.isDirectory
    }

    fun getFileInfo(diskShare: DiskShare, filePath: String): FileStandardInformation? {
        return openDiskEntry(diskShare, filePath)
            ?.getFileInformation(FileStandardInformation::class.java)
    }

    private fun openDiskEntry(share: DiskShare, filePath: String): DiskEntry? {
        return share.open(
            filePath,
            EnumSet.of(AccessMask.GENERIC_READ),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null
        )
    }
}