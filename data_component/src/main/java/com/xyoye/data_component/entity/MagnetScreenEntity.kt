package com.xyoye.data_component.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.xyoye.data_component.enums.MagnetScreenType
import com.xyoye.data_component.helper.MagnetScreenConverter

/**
 * Created by xyoye on 2020/10/26.
 */

@Entity(tableName = "magnet_screen")
@TypeConverters(MagnetScreenConverter::class)
data class MagnetScreenEntity(

    @ColumnInfo(name = "screen_id")
    var screenId: Int,

    @ColumnInfo(name = "screen_name")
    var screenName: String,

    @ColumnInfo(name = "screen_type")
    var screenType: MagnetScreenType,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)