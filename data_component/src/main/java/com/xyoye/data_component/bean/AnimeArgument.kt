package com.xyoye.data_component.bean

import android.os.Parcelable
import com.xyoye.data_component.data.AnimeData
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2024/2/1
 */

@Parcelize
data class AnimeArgument(
    val id: Int = 0,
    val title: String = "",
    val imageUrl: String = ""
) : Parcelable {

    companion object {
        fun fromData(data: AnimeData): AnimeArgument {
            return AnimeArgument(
                data.animeId,
                data.animeTitle.orEmpty(),
                data.imageUrl.orEmpty()
            )
        }
    }
}