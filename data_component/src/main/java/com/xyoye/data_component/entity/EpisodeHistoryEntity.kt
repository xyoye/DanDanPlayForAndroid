package com.xyoye.data_component.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2024/2/1.
 */

@Parcelize
data class EpisodeHistoryEntity(

    @Embedded
    val entity: PlayHistoryEntity,

    @Relation(
        entity = MediaLibraryEntity::class,
        parentColumn = "storage_id",
        entityColumn = "id"
    )
    val library: MediaLibraryEntity?
) : Parcelable