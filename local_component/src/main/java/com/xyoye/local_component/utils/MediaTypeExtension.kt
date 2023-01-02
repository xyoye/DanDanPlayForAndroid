package com.xyoye.local_component.utils

import com.xyoye.data_component.enums.MediaType
import com.xyoye.local_component.R

/**
 * Created by xyoye on 2021/1/18.
 */

fun MediaType.getCover() : Int {
    return when(this){
        MediaType.LOCAL_STORAGE -> R.drawable.ic_local_storage
        MediaType.STREAM_LINK -> R.drawable.ic_stream_link
        MediaType.MAGNET_LINK -> R.drawable.ic_magnet_link
        MediaType.FTP_SERVER -> R.drawable.ic_ftp_storage
        MediaType.WEBDAV_SERVER -> R.drawable.ic_webdav_storage
        MediaType.SMB_SERVER -> R.drawable.ic_smb_storage
        MediaType.REMOTE_STORAGE -> R.drawable.ic_remote_storage
        MediaType.OTHER_STORAGE -> R.drawable.ic_play_history
        MediaType.SCREEN_CAST -> R.drawable.ic_screencast
        MediaType.EXTERNAL_STORAGE -> R.drawable.ic_external_storage
    }
}