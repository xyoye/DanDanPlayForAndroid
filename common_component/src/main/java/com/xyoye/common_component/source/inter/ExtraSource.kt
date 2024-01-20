package com.xyoye.common_component.source.inter

import com.xyoye.data_component.bean.LocalDanmuBean

/**
 * Created by xyoye on 2021/11/14.
 *
 * 扩展资源，弹幕+字幕
 */

interface ExtraSource {
    fun getDanmu(): LocalDanmuBean?

    fun setDanmu(danmu: LocalDanmuBean?)

    fun getSubtitlePath(): String?

    fun setSubtitlePath(path: String?)
}