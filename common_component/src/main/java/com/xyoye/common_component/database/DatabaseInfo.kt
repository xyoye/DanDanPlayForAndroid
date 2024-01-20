package com.xyoye.common_component.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xyoye.common_component.database.dao.AnimeSearchHistoryDao
import com.xyoye.common_component.database.dao.DanmuBlockDao
import com.xyoye.common_component.database.dao.ExtendFolderDao
import com.xyoye.common_component.database.dao.MagnetScreenDao
import com.xyoye.common_component.database.dao.MagnetSearchHistoryDao
import com.xyoye.common_component.database.dao.MediaLibraryDao
import com.xyoye.common_component.database.dao.PlayHistoryDao
import com.xyoye.common_component.database.dao.VideoDao
import com.xyoye.data_component.entity.AnimeSearchHistoryEntity
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.data_component.entity.ExtendFolderEntity
import com.xyoye.data_component.entity.MagnetScreenEntity
import com.xyoye.data_component.entity.MagnetSearchHistoryEntity
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.entity.VideoEntity

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
    version = 12,
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