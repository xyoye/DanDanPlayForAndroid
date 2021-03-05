package com.xyoye.common_component.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xyoye.data_component.entity.ExtendFolderEntity

/**
 * Created by xyoye on 2021/2/22.
 */

@Dao
interface ExtendFolderDao {

    @Query("SELECT * FROM extend_folder")
    suspend fun getAll(): MutableList<ExtendFolderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg entity: ExtendFolderEntity)

    @Query("DELETE FROM extend_folder WHERE folder_path = (:folderPath)")
    suspend fun delete(folderPath: String)
}