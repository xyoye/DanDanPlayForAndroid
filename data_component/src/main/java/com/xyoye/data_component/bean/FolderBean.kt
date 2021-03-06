package com.xyoye.data_component.bean

import androidx.room.ColumnInfo
import androidx.room.Ignore

/**
 * Created by xyoye on 2020/7/29.
 */

data class FolderBean(

    @ColumnInfo(name = "folder_path")
    var folderPath: String,

    @ColumnInfo(name = "file_count")
    var fileCount: Int,

    @ColumnInfo(name = "filter")
    var isFilter: Boolean = false
) {
    @Ignore
    var isLastPlay: Boolean = false
}