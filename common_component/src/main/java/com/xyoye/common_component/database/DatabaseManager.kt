package com.xyoye.common_component.database

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.xyoye.common_component.base.app.BaseApplication

/**
 * Created by xyoye on 2020/7/29.
 */


class DatabaseManager private constructor() {
    //"CREATE UNIQUE INDEX IF NOT EXISTS index_anime_search_history_search_text ON anime_search_history(search_text)"
    //"CREATE TABLE anime_search_history( id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, search_text TEXT NOT NULL UNIQUE, search_time INTEGER NOT NULL)"
    //"ALTER TABLE search_history RENAME TO magnet_search_history"
    //"ALTER TABLE magnet_screen ADD COLUMN screen_id INTEGER NOT NULL"

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE media_library ADD COLUMN remote_secret TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP INDEX IF EXISTS 'index_media_library_url'")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_media_library_url_media_type ON media_library(url, media_type)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {

            override fun migrate(database: SupportSQLiteDatabase) {
                //新建临时表
                database.execSQL(
                    "CREATE TABLE play_history_temp(" +
                            "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                            "video_name TEXT NOT NULL," +
                            "url TEXT NOT NULL UNIQUE, " +
                            "media_type TEXT NOT NULL," +
                            "video_position INTEGER NOT NULL," +
                            "video_duration INTEGER NOT NULL," +
                            "play_time INTEGER NOT NULL," +
                            "danmu_path TEXT," +
                            "episode_id INTEGER NOT NULL," +
                            "subtitle_path TEXT," +
                            "extra TEXT)"
                )
                //旧表数据迁移
                database.execSQL(
                    "INSERT INTO play_history_temp(id, video_name, url, media_type, video_position, video_duration, play_time, danmu_path, episode_id,subtitle_path) " +
                            "SELECT id, video_name, url, media_type, video_position, video_duration, play_time, danmu_path, episode_id,subtitle_path FROM play_history"
                )
                //移除旧表
                database.execSQL("DROP TABLE play_history")
                //重命名为旧表
                database.execSQL("ALTER TABLE play_history_temp RENAME TO play_history")
                //加上唯一约束
                database.execSQL("DROP INDEX IF EXISTS 'index_play_history_url'")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_play_history_url ON play_history(url)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE media_library ADD COLUMN web_dav_strict INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE play_history ADD COLUMN torrent_path TEXT")
                database.execSQL("ALTER TABLE play_history ADD COLUMN torrent_index INTEGER NOT NULL DEFAULT -1")
                database.execSQL("ALTER TABLE play_history ADD COLUMN http_header TEXT")
            }
        }

        val instance = DatabaseManager.holder.database
    }

    private object DatabaseManager {
        val holder = DatabaseManager()
    }

    private var database = Room.databaseBuilder(
        BaseApplication.getAppContext(),
        DatabaseInfo::class.java,
        "rood_db"
    ).addMigrations(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6
    ).build()

}