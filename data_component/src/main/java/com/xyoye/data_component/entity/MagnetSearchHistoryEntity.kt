package com.xyoye.data_component.entity

import androidx.room.*
import com.xyoye.data_component.helper.DateConverter
import java.util.*

/**
 * Created by xyoye on 2020/8/18.
 */

@Entity(tableName = "magnet_search_history", indices = [Index(value = arrayOf("search_text"), unique = true)])
@TypeConverters(DateConverter::class)
data class MagnetSearchHistoryEntity(

    @ColumnInfo(name = "search_text")
    var searchText: String,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "search_time")
    var searchTime: Date = Date()
)