package com.xyoye.common_component.services

import com.xyoye.common_component.storage.file.StorageFile

/**
 * Created by xyoye on 2023/4/14
 */

interface StorageFileProvider {

    fun getShareStorageFile(): StorageFile?
}