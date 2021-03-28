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
        val MIGRATION_1_2 = object : Migration(1, 2){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE media_library ADD COLUMN remote_secret TEXT")
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
    ).addMigrations(MIGRATION_1_2).build()

}