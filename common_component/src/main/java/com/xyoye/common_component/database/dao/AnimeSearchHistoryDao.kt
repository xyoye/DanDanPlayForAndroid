package com.xyoye.common_component.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.xyoye.data_component.entity.AnimeSearchHistoryEntity
import com.xyoye.data_component.helper.DateConverter

/**
 * Created by xyoye on 2020/10/13.
 */

@Dao
interface AnimeSearchHistoryDao {

    @Query("SELECT search_text FROM anime_search_history ORDER BY search_time DESC LIMIT 10 OFFSET 0")
    fun getAll(): LiveData<MutableList<String>>

    @Query("DELETE FROM anime_search_history WHERE search_text = (:searchText)")
    suspend fun deleteByText(searchText: String)

    @Query("DELETE FROM anime_search_history")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @TypeConverters(DateConverter::class)
    suspend fun insert(vararg entity: AnimeSearchHistoryEntity)
}