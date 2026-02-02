package com.xyoye.common_component.storage

import android.net.Uri
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.danmu.DanmuFinder
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.common_component.utils.getParentFolderName
import com.xyoye.common_component.utils.isDanmuFile
import com.xyoye.common_component.utils.subtitle.SubtitleFinder
import com.xyoye.common_component.utils.subtitle.SubtitleUtils
import com.xyoye.data_component.bean.LocalDanmuBean
import com.xyoye.data_component.data.DanmuEpisodeData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import java.io.File

/**
 * Created by xyoye on 2022/12/29
 */

abstract class AbstractStorage(
    libraryEntity: MediaLibraryEntity
) : Storage {

    override var library: MediaLibraryEntity = libraryEntity

    override var directory: StorageFile? = null

    override var directoryFiles: List<StorageFile> = emptyList()

    override var rootUri: Uri = Uri.parse(libraryEntity.url)

    override suspend fun openDirectory(file: StorageFile, refresh: Boolean): List<StorageFile> {
        this.directory = file
        this.directoryFiles = listFiles(file)
        return directoryFiles
    }

    override fun close() {
        //do nothing
    }

    /**
     * 在根路径中定位到相对路径或绝对路径
     */
    protected fun resolvePath(path: String): Uri {
        return if (path.startsWith(File.separator)) {
            rootUri.buildUpon().path(path).build()
        } else {
            rootUri.buildUpon().appendPath(path).build()
        }
    }

    override fun getNetworkHeaders(): Map<String, String>? {
        return null
    }

    abstract suspend fun listFiles(file: StorageFile): List<StorageFile>

    override suspend fun cacheDanmu(file: StorageFile): LocalDanmuBean? {
        val danmuName = getFileNameNoExtension(file.fileName())
        val danmuFileName = "$danmuName.xml"
        val danmuFile = directoryFiles.find {
            it.isFile() && isDanmuFile(it.fileName()) && it.fileName() == danmuFileName
        } ?: return null

        val inputStream = openFile(danmuFile) ?: return null
        val directoryName = getParentFolderName(danmuFile.filePath())
        val episode = DanmuEpisodeData(
            animeTitle = directoryName,
            episodeTitle = getFileNameNoExtension(file.fileName())
        )

        return DanmuFinder.instance.saveStream(episode, inputStream)
    }

    override suspend fun cacheSubtitle(file: StorageFile): String? {
        val subtitleFile = directoryFiles
            .filter { it.isFile() }
            .run {
                SubtitleFinder.preferred(this, file.fileName()) { it.fileName() }
            } ?: return null

        val inputStream = openFile(subtitleFile) ?: return null
        val directoryName = getParentFolderName(subtitleFile.filePath())
        return SubtitleUtils.saveSubtitle(subtitleFile.fileName(), inputStream, directoryName)
    }

    override fun supportSearch(): Boolean {
        return false
    }

    override suspend fun search(keyword: String): List<StorageFile> {
        return emptyList()
    }

    override suspend fun test(): Boolean {
        return true
    }

    override fun updateFileHistory(file: StorageFile, history: PlayHistoryEntity?) {
        directoryFiles
            .firstOrNull { it.uniqueKey() == file.uniqueKey() }
            ?.let { it.playHistory = history }
    }
}