package com.xyoye.common_component.extension

import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.data_component.entity.PlayHistoryEntity

/**
 * Created by xyoye on 2021/7/25.
 */


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