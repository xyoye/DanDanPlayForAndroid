package com.xyoye.stream_component.utils.storage

import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.comparator.FileNameComparator
import com.xyoye.common_component.utils.comparator.FileSizeComparator
import com.xyoye.data_component.enums.StorageSort
import java.util.Comparator

/**
 * Created by xyoye on 2023/3/31.
 */

class StorageSortOption {
    var sort = StorageSort.NAME
        private set
    var asc = true
        private set
    var directoryFirst = true
        private set

    init {
        sort = StorageSort.formValue(AppConfig.getStorageSortType())
        asc = AppConfig.isStorageSortAsc()
        directoryFirst = AppConfig.isStorageSortDirectoryFirst()
    }

    fun setSort(sort: StorageSort): Boolean {
        this.sort = sort
        AppConfig.putStorageSortType(sort.value)
        return true
    }

    fun changeAsc(): Boolean {
        this.asc = !asc
        AppConfig.putStorageSortAsc(asc)
        return true
    }

    fun changeDirectoryFirst(): Boolean {
        directoryFirst = !directoryFirst
        AppConfig.putStorageSortDirectoryFirst(directoryFirst)
        return true
    }

    fun createComparator(): Comparator<StorageFile> {
        return if (sort == StorageSort.NAME) {
            FileNameComparator(
                getName = { it.fileName() },
                isDirectory = { it.isDirectory() },
                asc,
                directoryFirst
            )
        } else {
            FileSizeComparator(
                getName = { it.fileName() },
                getSize = { it.fileLength() },
                isDirectory = { it.isDirectory() },
                asc,
                directoryFirst
            )
        }
    }
}