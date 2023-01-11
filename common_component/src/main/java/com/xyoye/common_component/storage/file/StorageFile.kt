package com.xyoye.common_component.storage.file

import com.xyoye.data_component.entity.PlayHistoryEntity

/**
 * Created by xyoye on 2022/12/29
 */

interface StorageFile {

    /**
     * 播放记录
     */
    var playHistory: PlayHistoryEntity?

    /**
     * 文件路径
     */
    fun filePath(): String

    /**
     * 文件完整Url
     */
    fun fileUrl(): String

    /**
     * 是否为文件夹
     */
    fun isDirectory(): Boolean

    /**
     * 是否为文件
     */
    fun isFile(): Boolean

    /**
     * 文件名
     */
    fun fileName(): String

    /**
     * 文件在所有媒体库中的唯一值
     */
    fun uniqueKey(): String

    /**
     * 是否为根目录文件
     */
    fun isRootFile(): Boolean

    /**
     * 文件是否可读
     */
    fun canRead(): Boolean

    /**
     * 子文件数量
     */
    fun childFileCount(): Int

    /**
     * 获取真实文件
     */
    fun <T> getFile(): T?

    /**
     * 克隆一个对象
     */
    fun clone(): StorageFile

    /**
     * 关闭文件
     */
    fun close()
}

val StorageFile.danmu: Pair<String, Int>?
    get() = if (playHistory?.danmuPath.isNullOrEmpty()) {
        null
    } else {
        playHistory!!.danmuPath!! to playHistory!!.episodeId
    }

val StorageFile.subtitle: String?
    get() = if (playHistory?.subtitlePath.isNullOrEmpty()) {
        null
    } else {
        playHistory!!.subtitlePath!!
    }