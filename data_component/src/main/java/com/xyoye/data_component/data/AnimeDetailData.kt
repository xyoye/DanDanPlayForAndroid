package com.xyoye.data_component.data

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.xyoye.data_component.entity.EpisodeHistoryEntity
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/8/5.
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class AnimeDetailData(
    var bangumi: BangumiData? = null
) : CommonJsonData(), Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class BangumiData(
    var type: String? = null,
    var typeDescription: String? = null,
    var summary: String? = null,
    var metadata: MutableList<String>? = null,
    var bangumiUrl: String? = null,
    var userRating: Int,
    var favoriteStatus: String? = null,
    var comment: String? = null,
    var animeId: Int,
    var animeTitle: String? = null,
    var imageUrl: String? = null,
    var searchKeyword: String? = null,
    var isOnAir: Boolean,
    var airDay: Int,
    var isFavorited: Boolean,
    var isRestricted: Boolean,
    var rating: Double,
    var episodes: MutableList<EpisodeData> = mutableListOf(),
    var relateds: MutableList<AnimeData> = mutableListOf(),
    var similars: MutableList<AnimeData> = mutableListOf(),
    var tags: MutableList<TagData> = mutableListOf()
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class EpisodeData(
    val episodeId: String = "",
    val episodeTitle: String = "",
    val lastWatched: String? = null,
    val airDate: String? = null,

    @Json(ignore = true)
    val title: String = "",
    @Json(ignore = true)
    val subtitle: String = "",
    @Json(ignore = true)
    val searchEpisodeNum: String = "",

    @Json(ignore = true)
    val watchTime: String? = null,
    @Json(ignore = true)
    val histories: List<EpisodeHistoryEntity> = emptyList(),
    @Json(ignore = true)
    val isMarked: Boolean = false,
    @Json(ignore = true)
    val inMarkMode: Boolean = false
) : Parcelable {

    @Json(ignore = true)
    val markAble get() = lastWatched == null

    @Json(ignore = true)
    val watched get() = histories.isNotEmpty() || lastWatched != null
}

@Parcelize
@JsonClass(generateAdapter = true)
data class TagData(
    var id: Int, var name: String
) : Parcelable