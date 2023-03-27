package com.xyoye.common_component.storage.file

import com.xyoye.common_component.extension.toMd5String
import com.xyoye.common_component.storage.AbstractStorage
import com.xyoye.data_component.entity.PlayHistoryEntity

/**
 * Created by xyoye on 2022/12/29
 */

abstract class AbstractStorageFile(
    val storage: AbstractStorage
) : StorageFile {

    private val uniqueKey: String by lazy {
        val libraryId = storage.library.id
        val filePath = fileUrl()
        "$libraryId-$filePath".toMd5String()
    }

    override var playHistory: PlayHistoryEntity? = null

    override fun storagePath(): String {
        return filePath()
    }

    override fun fileLength(): Long {
        return 0
    }

    override fun uniqueKey(): String {
        return uniqueKey
    }

    override fun isFile(): Boolean {
        return isDirectory().not()
    }

    override fun isRootFile(): Boolean {
        return fileUrl() == storage.rootUri.toString()
    }

    override fun childFileCount(): Int {
        return 0
    }

    override fun <T> getFile(): T? {
        return getRealFile() as? T
    }

    override fun close() {
        //do nothing
    }

    override fun canRead(): Boolean {
        return true
    }

    override fun isVideoFile(): Boolean {
        return com.xyoye.common_component.utils.isVideoFile(fileName())
    }

    abstract fun getRealFile(): Any
}