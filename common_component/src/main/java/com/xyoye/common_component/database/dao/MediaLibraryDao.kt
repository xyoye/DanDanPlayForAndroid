package com.xyoye.common_component.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.data_component.helper.MediaTypeConverter

/**
 * Created by xyoye on 2021/1/18.
 */

@Dao
interface MediaLibraryDao {

    @Query("SELECT * FROM media_library ORDER BY id ASC")
    fun getAll(): LiveData<MutableList<MediaLibraryEntity>>

    @Query("SELECT * FROM media_library WHERE media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    fun getByMediaType(mediaType: MediaType): LiveData<MediaLibraryEntity>

    @Query("SELECT * FROM media_library WHERE media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun getByMediaTypeSuspend(mediaType: MediaType): MediaLibraryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg entities: MediaLibraryEntity)

    @Query("DELETE FROM media_library WHERE url = (:url) AND media_type = (:mediaType)")
    @TypeConverters(MediaTypeConverter::class)
    suspend fun delete(url: String, mediaType: MediaType)
}