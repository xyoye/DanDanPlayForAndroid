package com.xyoye.common_component.services

import com.alibaba.android.arouter.facade.template.IProvider
import com.xyoye.common_component.storage.file.StorageFile

/**
 * Created by xyoye on 2023/4/14
 */

interface StorageFileProvider : IProvider {

    fun getShareStorageFile(): StorageFile?
}