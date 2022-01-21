package com.xyoye.data_component.enums

/**
 * Created by xyoye on 2021/1/18.
 */

enum class MediaType(val value: String, val storageName: String) {
    LOCAL_STORAGE("local_storage", "本地媒体库"),

    OTHER_STORAGE("other_storage", "外部媒体库"),

    STREAM_LINK("stream_link", "串流视频"),

    MAGNET_LINK("magnet_link", "磁链视频"),

    FTP_SERVER("ftp_server", "FTP媒体库"),

    WEBDAV_SERVER("webdav_server", "WebDav媒体库"),

    SMB_SERVER("smb_server", "SMB媒体库"),

    REMOTE_STORAGE("remote_storage", "远程媒体库");

    companion object {
        fun fromValue(value: String): MediaType {
            return when (value) {
                "local_storage" -> LOCAL_STORAGE
                "stream_link" -> STREAM_LINK
                "magnet_link" -> MAGNET_LINK
                "ftp_server" -> FTP_SERVER
                "webdav_server" -> WEBDAV_SERVER
                "smb_server" -> SMB_SERVER
                "remote_storage" -> REMOTE_STORAGE
                else -> OTHER_STORAGE
            }
        }
    }
}