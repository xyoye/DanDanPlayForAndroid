package com.xyoye.common_component.source.helper


/**
 * Created by xyoye on 2022/1/6
 */
object SourceHelper {

    fun getHttpUniqueKey(url: String): String {
        return url.replaceFirst(
            "(http:|https:)//\\d+.\\d+.\\d+.\\d+(:\\d+)?".toRegex(),
            ""
        )
    }
}