package com.xyoye.common_component.storage.file

import com.xyoye.common_component.storage.Storage
import com.xyoye.data_component.bean.LocalDanmuBean
import com.xyoye.data_component.entity.PlayHistoryEntity

/**
 * Created by xyoye on 2022/12/29
 */

interface StorageFile {

    /**
     * 所属媒体库
     */
    var storage: Storage

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
     * 在Storage中的路径
     */
    fun storagePath(): String

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
     * 文件长度
     */
    fun fileLength(): Long

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
     * 是否为视频文件
     */
    fun isVideoFile(): Boolean

    /**
     * 是否为媒体库路径的父级路径
     */
    fun isStoragePathParent(childPath: String): Boolean

    /**
     * 关闭文件
     */
    fun close()

    /**
     * 视频时长
     */
    fun videoDuration(): Long
}

val StorageFile.danmu: LocalDanmuBean?
    get() = playHistory?.let {
        if (it.danmuPath.isNullOrEmpty()) null else LocalDanmuBean(it.danmuPath!!, it.episodeId)
    }

val StorageFile.subtitle: String?
    get() = if (playHistory?.subtitlePath.isNullOrEmpty()) {
        null
    } else {
        playHistory!!.subtitlePath!!
    }