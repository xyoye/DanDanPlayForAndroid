package com.xyoye.common_component.extension

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
 * Created by xyoye on 2023/1/15.
 */

fun DiskShare.openFile(path: String): File {
    return openFile(
        path,
        setOf(AccessMask.FILE_READ_DATA),
        setOf(FileAttributes.FILE_ATTRIBUTE_READONLY),
        setOf(SMB2ShareAccess.FILE_SHARE_READ),
        SMB2CreateDisposition.FILE_OPEN,
        setOf(SMB2CreateOptions.FILE_RANDOM_ACCESS)
    )
}

fun DiskShare.openDirectory(path: String): Directory {
    return openDirectory(
        path,
        EnumSet.of(AccessMask.FILE_LIST_DIRECTORY),
        setOf(FileAttributes.FILE_ATTRIBUTE_DIRECTORY),
        setOf(SMB2ShareAccess.FILE_SHARE_READ),
        SMB2CreateDisposition.FILE_OPEN,
        null
    )
}

fun DiskShare.open(path: String): DiskEntry {
    return open(
        path,
        EnumSet.of(AccessMask.GENERIC_READ),
        null,
        SMB2ShareAccess.ALL,
        SMB2CreateDisposition.FILE_OPEN,
        null
    )
}

fun DiskEntry.standardFileInfo(): FileStandardInformation {
    return getFileInformation(FileStandardInformation::class.java)
}