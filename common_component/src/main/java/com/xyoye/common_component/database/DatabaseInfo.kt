package com.xyoye.common_component.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xyoye.common_component.database.dao.*
import com.xyoye.data_component.entity.*

/**
 * Created by xyoye on 2020/7/29.
 */

@Database(
    entities =
    [VideoEntity::class,
        MagnetSearchHistoryEntity::class,
        AnimeSearchHistoryEntity::class,
        MagnetScreenEntity::class,
        MediaLibraryEntity::class,
        PlayHistoryEntity::class,
        DanmuBlockEntity::class,
        ExtendFolderEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class DatabaseInfo : RoomDatabase() {

    abstract fun getVideoDao(): VideoDao

    abstract fun getAnimeSearchHistoryDao(): AnimeSearchHistoryDao

    abstract fun getMagnetSearchHistoryDao(): MagnetSearchHistoryDao

    abstract fun getMagnetScreenDao(): MagnetScreenDao

    abstract fun getMediaLibraryDao(): MediaLibraryDao

    abstract fun getPlayHistoryDao(): PlayHistoryDao

    abstract fun getDanmuBlockDao(): DanmuBlockDao

    abstract fun getExtendFolderDao(): ExtendFolderDao
}