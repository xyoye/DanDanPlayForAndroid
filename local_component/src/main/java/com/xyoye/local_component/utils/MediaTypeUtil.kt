package com.xyoye.local_component.utils

import com.xyoye.data_component.enums.MediaType
import com.xyoye.local_component.R

/**
 * Created by xyoye on 2021/1/18.
 */

object MediaTypeUtil {

    fun getCover(mediaType: MediaType): Int {
        return when(mediaType){
            MediaType.LOCAL_STORAGE -> R.drawable.ic_local_storage
            MediaType.STREAM_LINK -> R.drawable.ic_stream_link
            MediaType.MAGNET_LINK -> R.drawable.ic_magnet_link
            MediaType.FTP_SERVER -> R.drawable.ic_ftp_storage
            MediaType.WEBDAV_SERVER -> R.drawable.ic_webdav_storage
            MediaType.SMB_SERVER -> R.drawable.ic_smb_storage
            MediaType.OTHER_STORAGE -> R.drawable.ic_play_history
        }
    }

    fun getText(mediaType: MediaType): String {
        return when (mediaType) {
            MediaType.STREAM_LINK -> "串流"
            MediaType.MAGNET_LINK -> "磁链"
            MediaType.LOCAL_STORAGE -> "本地"
            MediaType.FTP_SERVER -> "FTP"
            MediaType.WEBDAV_SERVER -> "Dav"
            MediaType.SMB_SERVER -> "SMB"
            MediaType.OTHER_STORAGE -> "其它"
        }
    }
}