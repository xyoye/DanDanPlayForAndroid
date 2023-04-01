package com.xyoye.common_component.storage

import android.net.Uri
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import java.io.InputStream

/**
 * Created by xyoye on 2022/12/29
 */

interface Storage {

    /**
     * 媒体库信息
     */
    var library: MediaLibraryEntity

    /**
     * 当前所在文件夹
     */
    var directory: StorageFile?

    /**
     * 当前所在文件夹文件列表
     */
    var directoryFiles: List<StorageFile>

    /**
     * 根Uri
     */
    var rootUri: Uri

    /**
     * 获取根目录文件
     */
    suspend fun getRootFile(): StorageFile?

    /**
     * 打开文件
     */
    suspend fun openFile(file: StorageFile): InputStream?

    /**
     * 打开文件夹
     */
    suspend fun openDirectory(file: StorageFile, refresh: Boolean): List<StorageFile>

    /**
     * 通过路径获取文件
     * @param path 文件路径，以'/'开头为绝对路径，否则为相对路径
     */
    suspend fun pathFile(path: String, isDirectory: Boolean): StorageFile?

    /**
     * 根据播放记录获取文件
     */
    suspend fun historyFile(history: PlayHistoryEntity): StorageFile?

    /**
     * 创建播放链接
     */
    suspend fun createPlayUrl(file: StorageFile): String?

    /**
     * 缓存弹幕文件到本地
     */
    suspend fun cacheDanmu(file: StorageFile): String?

    /**
     * 缓存字幕文件到本地
     */
    suspend fun cacheSubtitle(file: StorageFile): String?

    /**
     * 获取网络请求头
     */
    fun getNetworkHeaders(): Map<String, String>?

    /**
     * 是否支持搜索
     */
    fun supportSearch(): Boolean

    /**
     * 关键字搜索
     */
    suspend fun search(keyword: String): List<StorageFile>

    /**
     * 关闭媒体库
     */
    fun close()
}

val Storage.videoSources: List<StorageFile>
    get() = directoryFiles.filter { it.isVideoFile() }