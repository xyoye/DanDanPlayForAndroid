package com.xyoye.data_component.entity

import android.os.Parcelable
import androidx.room.*
import com.xyoye.data_component.enums.MediaType
import com.xyoye.data_component.helper.BooleanConverter
import com.xyoye.data_component.helper.MediaTypeConverter
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2021/1/18.
 */

@Parcelize
@Entity(tableName = "media_library", indices = [Index(value = arrayOf("url"), unique = true)])
@TypeConverters(BooleanConverter::class, MediaTypeConverter::class)
data class MediaLibraryEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "display_name")
    var displayName: String,

    @ColumnInfo(name = "url")
    var url: String,

    @ColumnInfo(name = "media_type")
    var mediaType: MediaType,

    @ColumnInfo(name = "account")
    var account: String? = null,

    @ColumnInfo(name = "password")
    var password: String? = null,

    @ColumnInfo(name = "is_anonymous")
    var isAnonymous: Boolean = false,

    @ColumnInfo(name = "port")
    var port: Int = 0,

    @ColumnInfo(name = "describe")
    var describe: String? = null,

    @ColumnInfo(name = "ftp_mode")
    var isActiveFTP: Boolean = false,

    @ColumnInfo(name = "ftp_address")
    var ftpAddress: String = "",

    @ColumnInfo(name = "ftp_encoding")
    var ftpEncoding: String = "UTF-8",

    @ColumnInfo(name = "smb_v2")
    var smbV2: Boolean = true,

    @ColumnInfo(name = "smb_share_path")
    var smbSharePath: String? = null
) : Parcelable