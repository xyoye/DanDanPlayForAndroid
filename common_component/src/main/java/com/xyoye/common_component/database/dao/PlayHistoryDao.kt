package com.xyoye.common_component.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.data_component.helper.MediaTypeConverter

/**
 * Created by xyoye on 2021/1/19.
 */

@Dao
interface PlayHistoryDao {

    @Query("SELECT * FROM play_history WHERE url != '' ORDER BY play_time DESC")
    fun getAll(): LiveData<MutableList<PlayHistoryEntity>>

    @Query("SELECT * FROM play_history WHERE url != '' AND media_type = (:mediaType) ORDER BY play_time DESC")
    @TypeConverters(MediaTypeConverter::class)
    fun getSingleMediaType(mediaType: MediaType): LiveData<MutableList<PlayHistoryEntity>>

    @Query("SELECT * FROM play_history WHERE media_type IN (:mediaTypes) ORDER BY play_time DESC LIMIT 1")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun gitLastPlay(vararg mediaTypes: MediaType): PlayHistoryEntity?

    @Query("SELECT * FROM play_history WHERE storage_id = (:storageId) ORDER BY play_time DESC LIMIT 1")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun gitStorageLastPlay(storageId: Int): PlayHistoryEntity?

    @Query(
        "SELECT history.*,(last_play.unique_key IS NOT NULL) as is_last_play" +
                " FROM (SELECT * FROM play_history WHERE unique_key = (:uniqueKey) AND media_type = (:mediaType)) as history" +
                " LEFT JOIN (SELECT unique_key FROM play_history WHERE media_type = (:mediaType) ORDER BY play_time DESC LIMIT 1) as last_play" +
                " ON history.unique_key == last_play.unique_key"
    )
    @TypeConverters(MediaTypeConverter::class)
    suspend fun getPlayHistory(uniqueKey: String, mediaType: MediaType): PlayHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg entities: PlayHistoryEntity)

    @Query("DELETE FROM play_history WHERE id = (:id)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun delete(id: Int)

    @Query("DELETE FROM play_history WHERE media_type IN (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun deleteTypeAll(mediaType: List<MediaType>)

    @Query("DELETE FROM play_history")
    suspend fun deleteAll()

    @Query("UPDATE play_history SET danmu_path = (:danmuPath), episode_id = (:episodeId) WHERE unique_key = (:uniqueKey) AND media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun updateDanmu(uniqueKey: String, mediaType: MediaType, danmuPath: String?, episodeId: Int)

    @Query("UPDATE play_history SET subtitle_path = (:subtitlePath) WHERE unique_key = (:uniqueKey) AND media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun updateSubtitle(uniqueKey: String, mediaType: MediaType, subtitlePath: String?)
}