package com.xyoye.common_component.source.media

import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.source.factory.StorageVideoSourceFactory
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.file.impl.TorrentStorageFile
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.utils.thunder.ThunderManager
import com.xyoye.data_component.bean.LocalDanmuBean
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2023/1/2.
 */

class StorageVideoSource(
    private val playUrl: String,
    private val file: StorageFile,
    private val videoSources: List<StorageFile>,
    private var danmu: LocalDanmuBean?,
    private var subtitlePath: String?,
    private var audioPath: String?,
) : BaseVideoSource(
    videoSources.indexOfFirst { it.uniqueKey() == file.uniqueKey() },
    videoSources
) {

    override fun getDanmu(): LocalDanmuBean? {
        return danmu
    }

    override fun setDanmu(danmu: LocalDanmuBean?) {
        this.danmu = danmu
    }

    override fun getSubtitlePath(): String? {
        return subtitlePath
    }

    override fun setSubtitlePath(path: String?) {
        subtitlePath = path
    }

    override fun getAudioPath(): String? {
        return audioPath
    }

    override fun setAudioPath(path: String?) {
        audioPath = path
    }

    override fun indexTitle(index: Int): String {
        val fileName = videoSources[index].fileName()
        return getFileName(fileName)
    }

    override suspend fun indexSource(index: Int): BaseVideoSource? {
        return StorageVideoSourceFactory.create(
            videoSources[index]
        )
    }

    override fun getVideoUrl(): String {
        return playUrl
    }

    override fun getVideoTitle(): String {
        return file.fileName()
    }

    override fun getCurrentPosition(): Long {
        return file.playHistory?.videoPosition ?: 0
    }

    override fun getMediaType(): MediaType {
        return file.storage.library.mediaType
    }

    override fun getUniqueKey(): String {
        return file.uniqueKey()
    }

    override fun getHttpHeader(): Map<String, String>? {
        return file.storage.getNetworkHeaders()
    }

    override fun getStorageId(): Int {
        return file.storage.library.id
    }

    override fun getStoragePath(): String {
        return file.storagePath()
    }

    fun getTorrentPath(): String? {
        if (file is TorrentStorageFile) {
            return file.filePath()
        }
        return null
    }

    fun getTorrentIndex(): Int {
        if (file is TorrentStorageFile) {
            return file.getRealFile().mFileIndex
        }
        return -1
    }

    fun getPlayTaskId(): Long {
        if (file is TorrentStorageFile) {
            return ThunderManager.getInstance().getTaskId(file.filePath())
        }
        return -1L
    }
}