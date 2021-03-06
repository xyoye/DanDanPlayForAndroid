package com.xyoye.data_component.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.xyoye.data_component.helper.DateConverter
import java.util.*

@Entity(tableName = "danmu_block")
@TypeConverters(DateConverter::class)
data class DanmuBlockEntity(

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "keyword")
    val keyword: String,

    @ColumnInfo(name = "is_regex")
    val isRegex: Boolean = false,

    @ColumnInfo(name = "add_time")
    val addTime: Date = Date(),

    @ColumnInfo(name = "is_cloud")
    val isCloud: Boolean = false
)