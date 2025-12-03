package com.xyoye.storage_component.providers

import com.therouter.inject.ServiceProvider
import com.xyoye.common_component.services.StorageFileProvider
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.ActivityHelper
import com.xyoye.storage_component.ui.activities.storage_file.StorageFileActivity

/**
 * Created by xyoye on 2023/4/14
 */

@ServiceProvider
class StorageFileProviderImpl : StorageFileProvider {

    override fun getShareStorageFile(): StorageFile? {
        return ActivityHelper.instance.findActivity(StorageFileActivity::class.java)
            ?.run { this as? StorageFileActivity }
            ?.shareStorageFile
    }
}