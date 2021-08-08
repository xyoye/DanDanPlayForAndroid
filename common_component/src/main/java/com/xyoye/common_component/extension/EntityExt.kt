package com.xyoye.common_component.extension

import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.entity.PlayHistoryEntity

/**
 * Created by xyoye on 2021/7/25.
 */

fun PlayHistoryEntity.torrentPath(): String? {
    return extraMap()["torrent_path"]
}
fun PlayHistoryEntity.torrentTitle(): String? {
    return extraMap()["torrent_title"]
}
fun PlayHistoryEntity.torrentFileIndex(): Int {
    return extraMap()["torrent_file_index"]?.toIntOrNull() ?: -1
}
fun PlayHistoryEntity.getHttpHeader(): String {
    return extraMap()["http_header"] ?: ""
}

fun PlayHistoryEntity.extraMap(): Map<String, String> {
    var extraMap = getUnInitMap()
    if (extraMap != null)
        return extraMap

    extraMap = if (extra != null) {
        JsonHelper.parseJsonMap(extra!!)
    } else {
        emptyMap()
    }
    return extraMap
}