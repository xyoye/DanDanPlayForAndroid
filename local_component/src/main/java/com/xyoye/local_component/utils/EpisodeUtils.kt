package com.xyoye.local_component.utils

/**
 * Created by xyoye on 2020/11/26.
 */

private val animeType = mapOf(
    Pair("tvseries", "TV动画"),
    Pair("movie", "剧场版"),
    Pair("ova", "OVA"),
    Pair("jpdrama", "日 剧"),
    Pair("jpmovie", "日本电影"),
    Pair("web", "网络放送"),
    Pair("tvspecial", "TV特送"),
    Pair("unknown", "未知分类"),
    Pair("musicvideo", "MV"),
    Pair("other", "其 它")
)

fun getAnimeType(type: String?): String {
    if (type == null)
        return "其 它"
    return animeType[type] ?: "其 它"
}