package com.xyoye.stream_component.providers

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.services.StorageFileProvider
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.ActivityHelper
import com.xyoye.stream_component.ui.activities.storage_file.StorageFileActivity

/**
 * Created by xyoye on 2023/4/14
 */

@Route(path = RouteTable.Stream.StorageFileProvider, name = "媒体库文件共享提供者")
class StorageFileProviderImpl : StorageFileProvider {

    override fun init(context: Context?) {

    }

    override fun getShareStorageFile(): StorageFile? {
        return ActivityHelper.instance.findActivity(StorageFileActivity::class.java)
            ?.run { this as? StorageFileActivity }
            ?.shareStorageFile
    }
}