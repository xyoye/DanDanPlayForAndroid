package com.xyoye.common_component.source.media

import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.source.factory.StorageVideoSourceFactory
import com.xyoye.common_component.storage.Storage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.storage.videoSources
import com.xyoye.common_component.utils.getFileName
import com.xyoye.data_component.enums.MediaType

/**
 * Created by xyoye on 2023/1/2.
 */

class StorageVideoSource(
    private val playUrl: String,
    private val file: StorageFile,
    private val storage: Storage,
    private var episodeId: Int,
    private var danmuPath: String?,
    private var subtitlePath: String?,
) : BaseVideoSource(
    storage.videoSources.indexOf(file),
    storage.videoSources
) {

    override fun getDanmuPath(): String? {
        return danmuPath
    }

    override fun setDanmuPath(path: String) {
        danmuPath = path
    }

    override fun getEpisodeId(): Int {
        return episodeId
    }

    override fun setEpisodeId(id: Int) {
        episodeId = id
    }

    override fun getSubtitlePath(): String? {
        return subtitlePath
    }

    override fun setSubtitlePath(path: String) {
        subtitlePath = path
    }

    override fun indexTitle(index: Int): String {
        val fileName = storage.videoSources[index].fileName()
        return getFileName(fileName)
    }

    override suspend fun indexSource(index: Int): BaseVideoSource? {
        return StorageVideoSourceFactory.create(
            storage.videoSources[index],
            storage
        )
    }

    override fun getVideoUrl(): String {
        return playUrl
    }

    override fun getVideoTitle(): String {
        return getFileName(file.fileName())
    }

    override fun getCurrentPosition(): Long {
        return file.playHistory?.videoPosition ?: 0
    }

    override fun getMediaType(): MediaType {
        return storage.library.mediaType
    }

    override fun getUniqueKey(): String {
        return file.uniqueKey()
    }

    override fun getHttpHeader(): Map<String, String>? {
        return storage.getNetworkHeaders()
    }

    override fun getStorageId(): Int {
        return storage.library.id
    }

    override fun getStoragePath(): String {
        return file.storagePath()
    }
}