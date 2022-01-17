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

    @Query("SELECT * FROM play_history ORDER BY play_time DESC")
    fun getAll(): LiveData<MutableList<PlayHistoryEntity>>

    @Query("SELECT * FROM play_history WHERE media_type IN (:mediaTypes) ORDER BY play_time DESC")
    @TypeConverters(MediaTypeConverter::class)
    fun getMultipleMediaType(mediaTypes: Array<MediaType>): LiveData<MutableList<PlayHistoryEntity>>

    @Query("SELECT * FROM play_history WHERE media_type = (:mediaType) ORDER BY play_time DESC")
    @TypeConverters(MediaTypeConverter::class)
    fun getSingleMediaType(mediaType: MediaType): LiveData<MutableList<PlayHistoryEntity>>

    @Query("SELECT * FROM play_history WHERE media_type IN (:mediaTypes) ORDER BY play_time DESC LIMIT 1")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun gitLastPlay(vararg mediaTypes: MediaType): PlayHistoryEntity?

    @Query("SELECT * FROM play_history WHERE media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun findMagnetPlay(mediaType: MediaType): MutableList<PlayHistoryEntity>

    @Query("SELECT * FROM play_history WHERE media_type = (:mediaTypes) ORDER BY play_time DESC LIMIT 1")
    @TypeConverters(MediaTypeConverter::class)
    fun gitLastPlayLiveData(mediaTypes: MediaType): LiveData<PlayHistoryEntity?>

    @Query("SELECT * FROM play_history WHERE media_type = (:mediaType) ORDER BY play_time DESC")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun getByMediaType(mediaType: MediaType): MutableList<PlayHistoryEntity>

    @Query("SELECT * FROM play_history WHERE url = (:url) AND media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun getPlayHistory(url: String, mediaType: MediaType): PlayHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg entities: PlayHistoryEntity)

    @Query("DELETE FROM play_history WHERE url = (:url) AND media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun delete(url: String, mediaType: MediaType)

    @Query("DELETE FROM play_history WHERE media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun deleteTypeAll(mediaType: MediaType)

    @Query("UPDATE play_history SET danmu_path = (:danmuPath), episode_id = (:episodeId) WHERE url = (:url) AND media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun updateDanmu(url: String, mediaType: MediaType, danmuPath: String?, episodeId: Int)

    @Query("UPDATE play_history SET subtitle_path = (:subtitlePath) WHERE url = (:url) AND media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun updateSubtitle(url: String, mediaType: MediaType, subtitlePath: String?)

    @Query("SELECT video_position FROM play_history WHERE url = (:url) AND media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun getPlayHistoryPosition(url: String, mediaType: MediaType): Long?

    @Query("SELECT * FROM play_history WHERE unique_key = (:uniqueKey) AND media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun getHistoryByKey(uniqueKey: String, mediaType: MediaType): PlayHistoryEntity?

    @Query("UPDATE play_history SET danmu_path = (:danmuPath), episode_id = (:episodeId) WHERE unique_key = (:uniqueKey) AND media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun updateDanmuByKey(uniqueKey: String, mediaType: MediaType, danmuPath: String?, episodeId: Int)

    @Query("UPDATE play_history SET subtitle_path = (:subtitlePath) WHERE unique_key = (:uniqueKey) AND media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun updateSubtitleByKey(uniqueKey: String, mediaType: MediaType, subtitlePath: String?)
}