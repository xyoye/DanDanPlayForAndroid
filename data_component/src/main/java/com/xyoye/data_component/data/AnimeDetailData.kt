package com.xyoye.data_component.data

import android.os.Parcelable
import com.xyoye.data_component.entity.EpisodeHistoryEntity
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Created by xyoye on 2020/8/5.
 */

@Parcelize
@Serializable
data class AnimeDetailData(
    var bangumi: BangumiData? = null
) : CommonJsonData(), Parcelable

@Parcelize
@Serializable
data class BangumiData(
    var type: String? = null,
    var typeDescription: String? = null,
    var summary: String? = null,
    var metadata: List<String> = emptyList(),
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
    var episodes: List<EpisodeData> = emptyList(),
    var relateds: List<AnimeData> = emptyList(),
    var similars: List<AnimeData> = emptyList(),
    var tags: List<TagData> = emptyList()
) : Parcelable

@Parcelize
@Serializable
data class EpisodeData(
    val episodeId: String = "",
    val episodeTitle: String = "",
    val lastWatched: String? = null,
    val airDate: String? = null,

    @Transient
    val title: String = "",
    @Transient
    val subtitle: String = "",
    @Transient
    val searchEpisodeNum: String = "",

    @Transient
    val watchTime: String? = null,
    @Transient
    val histories: List<EpisodeHistoryEntity> = emptyList(),
    @Transient
    val isMarked: Boolean = false,
    @Transient
    val inMarkMode: Boolean = false
) : Parcelable {

    val markAble get() = lastWatched == null

    val watched get() = histories.isNotEmpty() || lastWatched != null
}

@Parcelize
@Serializable
data class TagData(
    var id: Int, var name: String
) : Parcelable
