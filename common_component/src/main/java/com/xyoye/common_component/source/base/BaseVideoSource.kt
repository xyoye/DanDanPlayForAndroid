package com.xyoye.common_component.source.base

import com.xyoye.common_component.source.inter.ExtraSource
import com.xyoye.common_component.source.inter.VideoSource
import com.xyoye.data_component.bean.LocalDanmuBean


/**
 * Created by xyoye on 2022/1/11
 */
abstract class BaseVideoSource(
    index: Int,
    videoSources: List<*>
) : GroupVideoSource(index, videoSources), VideoSource, ExtraSource {
    override fun getDanmu(): LocalDanmuBean? {
        return null
    }

    override fun setDanmu(danmu: LocalDanmuBean?) {

    }

    override fun getSubtitlePath(): String? {
        return null
    }

    override fun setSubtitlePath(path: String?) {

    }

    override fun getAudioPath(): String? {
        return null
    }

    override fun setAudioPath(path: String?) {

    }

    override fun getHttpHeader(): Map<String, String>? {
        return null
    }

    override fun getStorageId(): Int? {
        return null
    }

    override fun getStoragePath(): String? {
        return null
    }
}