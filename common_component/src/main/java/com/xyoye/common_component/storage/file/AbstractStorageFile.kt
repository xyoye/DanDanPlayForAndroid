package com.xyoye.common_component.storage.file

import com.xyoye.common_component.extension.toMd5String
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.common_component.storage.AbstractStorage

/**
 * Created by xyoye on 2022/12/29
 */

abstract class AbstractStorageFile(
    val storage: AbstractStorage
) : StorageFile {

    override var playHistory: PlayHistoryEntity? = null

    override fun uniqueKey(): String {
        val libraryId = storage.library.id
        val filePath = fileUrl()
        return "$libraryId-$filePath".toMd5String()
    }

    override fun isFile(): Boolean {
        return isDirectory().not()
    }

    override fun isRootFile(): Boolean {
        return fileUrl() == storage.getRootUrl()
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

    abstract fun getRealFile(): Any
}