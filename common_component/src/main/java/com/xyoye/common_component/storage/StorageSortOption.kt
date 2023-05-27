package com.xyoye.common_component.storage

import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.comparator.FileNameComparator
import com.xyoye.common_component.utils.comparator.FileSizeComparator
import com.xyoye.data_component.enums.StorageSort

/**
 * Created by xyoye on 2023/3/31.
 */

object StorageSortOption {

    fun getSort(): StorageSort {
        return StorageSort.formValue(AppConfig.getStorageSortType())
    }

    fun setSort(sort: StorageSort): Boolean {
        AppConfig.putStorageSortType(sort.value)
        return true
    }

    fun isAsc(): Boolean {
        return AppConfig.isStorageSortAsc()
    }

    fun changeAsc(): Boolean {
        AppConfig.putStorageSortAsc(!isAsc())
        return true
    }

    fun isDirectoryFirst(): Boolean {
        return AppConfig.isStorageSortDirectoryFirst()
    }

    fun changeDirectoryFirst(): Boolean {
        AppConfig.putStorageSortDirectoryFirst(!isDirectoryFirst())
        return true
    }

    fun comparator(): Comparator<StorageFile> {
        val sort = getSort()
        val asc = isAsc()
        val directoryFirst = isDirectoryFirst()

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