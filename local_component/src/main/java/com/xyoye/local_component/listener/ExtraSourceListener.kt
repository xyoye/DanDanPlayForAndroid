package com.xyoye.local_component.listener

import com.xyoye.common_component.storage.file.StorageFile

/**
 * Created by xyoye on 2020/10/20.
 */

interface ExtraSourceListener {
    fun search(searchText: String)

    fun onStorageFileChanged(storageFile: StorageFile)
}