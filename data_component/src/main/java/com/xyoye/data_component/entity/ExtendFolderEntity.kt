package com.xyoye.data_component.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by xyoye on 2021/2/22.
 */


@Entity(tableName = "extend_folder")
data class ExtendFolderEntity(

    @PrimaryKey
    @ColumnInfo(name = "folder_path")
    var folderPath: String,

    @ColumnInfo(name = "child_count")
    var childCount: Int
)