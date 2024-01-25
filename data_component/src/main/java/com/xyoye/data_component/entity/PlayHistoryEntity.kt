package com.xyoye.data_component.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.xyoye.data_component.enums.MediaType
import com.xyoye.data_component.helper.BooleanConverter
import com.xyoye.data_component.helper.DateConverter
import com.xyoye.data_component.helper.MediaTypeConverter
import java.util.Date

/**
 * Created by xyoye on 2021/1/19.
 */

@Entity(tableName = "play_history", indices = [Index(value = arrayOf("unique_key", "storage_id"), unique = true)])
@TypeConverters(DateConverter::class, MediaTypeConverter::class, BooleanConverter::class)
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "video_name")
    val videoName: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "media_type")
    val mediaType: MediaType,

    @ColumnInfo(name = "video_position")
    var videoPosition: Long = 0,

    @ColumnInfo(name = "video_duration")
    var videoDuration: Long = 0,

    @ColumnInfo(name = "play_time")
    var playTime: Date = Date(),

    @ColumnInfo(name = "danmu_path")
    var danmuPath: String? = null,

    @ColumnInfo(name = "episode_id")
    var episodeId: String? = null,

    @ColumnInfo(name = "subtitle_path")
    var subtitlePath: String? = null,

    @ColumnInfo(name = "torrent_path")
    var torrentPath: String? = null,

    @ColumnInfo(name = "torrent_index")
    var torrentIndex: Int = -1,

    @ColumnInfo(name = "http_header")
    var httpHeader: String? = null,

    @ColumnInfo(name = "unique_key")
    var uniqueKey: String = "",

    @ColumnInfo(name = "storage_path")
    var storagePath: String? = null,

    @ColumnInfo(name = "storage_id")
    var storageId: Int? = null,

    @ColumnInfo(name = "audio_path")
    var audioPath: String? = null
) {
    @Ignore
    var isLastPlay: Boolean = false
}