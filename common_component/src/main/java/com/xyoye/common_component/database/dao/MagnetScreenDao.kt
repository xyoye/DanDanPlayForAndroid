package com.xyoye.common_component.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.xyoye.data_component.entity.MagnetScreenEntity
import com.xyoye.data_component.enums.MagnetScreenType
import com.xyoye.data_component.helper.DateConverter
import com.xyoye.data_component.helper.MagnetScreenConverter

/**
 * Created by xyoye on 2020/10/13.
 */

@Dao
interface MagnetScreenDao {

    @Query("SELECT * FROM magnet_screen WHERE screen_type = (:screenType)  ORDER BY screen_name ASC")
    @TypeConverters(MagnetScreenConverter::class)
    fun getAll(screenType: MagnetScreenType): LiveData<MutableList<MagnetScreenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @TypeConverters(DateConverter::class)
    suspend fun insert(vararg entity: MagnetScreenEntity)
}