package com.xyoye.common_component.extension

import com.xyoye.common_component.config.RouteTable
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2023/1/3
 */

val MediaType.editRoute: String?
    get() = when (this) {
        MediaType.FTP_SERVER -> RouteTable.Stream.FTPLogin
        MediaType.WEBDAV_SERVER -> RouteTable.Stream.WebDavLogin
        MediaType.SMB_SERVER -> RouteTable.Stream.SmbLogin
        MediaType.REMOTE_STORAGE -> RouteTable.Stream.StoragePlus
        MediaType.SCREEN_CAST -> RouteTable.Stream.ScreencastConnect
        MediaType.EXTERNAL_STORAGE -> RouteTable.Stream.StoragePlus
        else -> null
    }

val MediaType.deletable: Boolean
    get() = when (this) {
        MediaType.LOCAL_STORAGE,
        MediaType.STREAM_LINK,
        MediaType.MAGNET_LINK,
        MediaType.OTHER_STORAGE -> false
        else -> true
    }