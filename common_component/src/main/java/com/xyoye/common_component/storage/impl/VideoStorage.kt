package com.xyoye.common_component.storage.impl

import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.toFile
import com.xyoye.common_component.resolver.MediaResolver
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.impl.VideoStorageFile
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.common_component.utils.isDanmuFile
import com.xyoye.common_component.utils.isSubtitleFile
import com.xyoye.common_component.utils.meida.VideoScan
import com.xyoye.common_component.utils.subtitle.SubtitleFinder
import com.xyoye.data_component.bean.FolderBean
import com.xyoye.data_component.bean.LocalDanmuBean
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.entity.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.InputStream

/**
 * Created by xyoye on 2023/3/22
 */

class VideoStorage(library: MediaLibraryEntity) : AbstractStorage(library) {

    override suspend fun openDirectory(file: StorageFile, refresh: Boolean): List<StorageFile> {
        if (refresh) {
            deepRefresh()
        }
        return super.openDirectory(file, refresh)
    }

    override suspend fun listFiles(file: StorageFile): List<StorageFile> {
        var childFiles = openStorageDirectory(file)
        if (childFiles.isEmpty() && file.isRootFile()) {
            // 第一次打开媒体库时，文件尚未录入数据库，需要先做一次扫描
            deepRefresh()
            childFiles = openStorageDirectory(file)
        }
        return childFiles
    }

    override suspend fun getRootFile(): StorageFile {
        val folderBean = FolderBean(rootUri.toString(), 0)
        return VideoStorageFile(this, folderBean)
    }

    override suspend fun openFile(file: StorageFile): InputStream {
        return withContext(Dispatchers.IO) {
            FileInputStream(file.filePath())
        }
    }

    override suspend fun pathFile(path: String, isDirectory: Boolean): StorageFile? {
        if (isDirectory.not()) {
            val videoEntity = DatabaseManager.instance.getVideoDao().getVideo(path)
                ?: return null
            return VideoStorageFile(this, videoEntity)
        }
        return null
    }

    override suspend fun historyFile(history: PlayHistoryEntity): StorageFile? {
        val storagePath = history.storagePath ?: return null
        return pathFile(storagePath, false)?.also {
            it.playHistory = history
        }
    }

    override suspend fun createPlayUrl(file: StorageFile): String {
        return file.filePath()
    }

    override suspend fun cacheDanmu(file: StorageFile): LocalDanmuBean? {
        val danmuFileName = getFileNameNoExtension(file.fileName()) + ".xml"
        return file.getFile<VideoEntity>()
            ?.folderPath.toFile()
            ?.listFiles()
            ?.filter { it.isFile && isDanmuFile(it.absolutePath) }
            ?.find { getFileName(it.absolutePath) == danmuFileName }
            ?.absolutePath
            ?.let { LocalDanmuBean(it) }
    }

    override suspend fun cacheSubtitle(file: StorageFile): String? {
        return file.getFile<VideoEntity>()
            ?.folderPath.toFile()
            ?.listFiles()
            ?.filter { it.isFile && isSubtitleFile(it.absolutePath) }
            ?.run { SubtitleFinder.preferred(this, file.fileName()) { getFileName(it) } }
            ?.absolutePath
    }

    override fun supportSearch(): Boolean {
        return true
    }

    override suspend fun search(keyword: String): List<StorageFile> {
        if (keyword.isEmpty()) {
            return openDirectory(directory ?: getRootFile(), false)
        }
        return DatabaseManager.instance.getVideoDao().getAll()
            .filter { it.filePath.contains(keyword) }
            .map { VideoStorageFile(this, it) }
    }

    private suspend fun openStorageDirectory(storageFile: StorageFile): List<StorageFile> {
        return if (storageFile.isRootFile()) {
            DatabaseManager.instance
                .getVideoDao()
                .getFolderByFilter()
                .map { VideoStorageFile(this, it) }
        } else {
            DatabaseManager.instance
                .getVideoDao()
                .getVideoInFolder(storageFile.filePath())
                .map { VideoStorageFile(this, it) }
        }
    }

    private suspend fun deepRefresh() {
        //系统视频数据 = 自定义扫描目录视频 + MediaStore中系统视频
        val systemVideos = DatabaseManager.instance.getExtendFolderDao().getAll()
            .flatMap { VideoScan.traverse(it.folderPath) }
            .plus(MediaResolver.queryVideo())
            .distinctBy { it.filePath }

        //数据库视频数据
        val databaseVideos = DatabaseManager.instance.getVideoDao().getAll()

        //从数据库中移除，不在系统视频数据中的数据库视频
        databaseVideos
            .map { it.filePath }
            .filterNot { filePath -> systemVideos.any { it.filePath == filePath } }
            .let { DatabaseManager.instance.getVideoDao().deleteByPaths(it) }

        //往数据库中添加，不在数据库视频中的系统视频数据
        systemVideos.filterNot { video -> databaseVideos.any { it.filePath == video.filePath } }
            .let { DatabaseManager.instance.getVideoDao().insert(*it.toTypedArray()) }
    }
}