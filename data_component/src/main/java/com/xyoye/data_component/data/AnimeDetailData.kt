package com.xyoye.data_component.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by xyoye on 2020/8/5.
 */

@Parcelize
data class AnimeDetailData(
    var bangumi: BangumiData? = null
) : CommonJsonData(), Parcelable

@Parcelize
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
data class EpisodeData(
    var episodeId: Int,
    var episodeTitle: String? = null,
    var lastWatched: String? = null,
    var airDate: String? = null
) : Parcelable

@Parcelize
data class TagData(
    var id: Int, var name: String
) : Parcelable