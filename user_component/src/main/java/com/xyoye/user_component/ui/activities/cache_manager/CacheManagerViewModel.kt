package com.xyoye.user_component.ui.activities.cache_manager

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.utils.*
import com.xyoye.data_component.bean.CacheBean
import com.xyoye.data_component.enums.CacheType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class CacheManagerViewModel : BaseViewModel() {

    private val appCacheDir = BaseApplication.getAppContext().cacheDir
    private val externalCacheDir = File(PathHelper.getCachePath())

    val systemCachePath = appCacheDir.absolutePath ?: ""
    val externalCachePath = externalCacheDir.absolutePath ?: ""

    val systemCacheSizeText = ObservableField("")
    val externalCacheSizeText = ObservableField("")

    val cacheDirsLiveData = MutableLiveData<List<CacheBean>>()

    fun refreshCache() {
        val appCacheDirSize = IOUtils.getDirectorySize(appCacheDir)
        systemCacheSizeText.set(formatFileSize(appCacheDirSize))

        val externalCacheDirSize = IOUtils.getDirectorySize(externalCacheDir)
        externalCacheSizeText.set(formatFileSize(externalCacheDirSize))

        val cacheDirs = mutableListOf<CacheBean>()
        CacheType.values().forEach {
            var fileCount = 0
            if (it == CacheType.DANMU_CACHE) {
                fileCount = getDanmuFileCount(PathHelper.getDanmuDirectory())
            } else if (it == CacheType.SUBTITLE_CACHE) {
                fileCount = getSubtitleFileCount(PathHelper.getSubtitleDirectory())
            }
            val cacheBean = CacheBean(it, fileCount, getCacheSize(it))
            cacheDirs.add(cacheBean)
        }

        val otherCacheBean = CacheBean(null, 0, getCacheSize(null))
        cacheDirs.add(otherCacheBean)

        cacheDirsLiveData.postValue(cacheDirs)
    }

    fun clearAppCache() {
        viewModelScope.launch(Dispatchers.IO) {
            clearCacheDirectory(appCacheDir)
        }
    }

    fun clearCacheByType(cacheType: CacheType?) {
        if (cacheType != null) {
            viewModelScope.launch(Dispatchers.IO) {
                clearCacheDirectory(PathHelper.getCacheDirectory(cacheType))
                refreshCache()
            }
            return
        }
        val childCacheDirs = externalCacheDir.listFiles()
            ?: return

        val namedCacheDirPaths = CacheType.values().map {
            PathHelper.getCacheDirectory(it).absolutePath
        }
        viewModelScope.launch(Dispatchers.IO) {
            childCacheDirs.forEach {
                if (it.absolutePath !in namedCacheDirPaths) {
                    clearCacheDirectory(it)
                }
            }
            refreshCache()
        }
    }

    private fun getCacheSize(cacheType: CacheType?): Long {
        return if (cacheType != null) {
            val cacheTypeDir = PathHelper.getCacheDirectory(cacheType)
            IOUtils.getDirectorySize(cacheTypeDir)
        } else {
            val totalCacheSize = IOUtils.getDirectorySize(externalCacheDir)
            var namedCacheSize = 0L
            CacheType.values().forEach {
                namedCacheSize += getCacheSize(it)
            }
            totalCacheSize - namedCacheSize
        }
    }

    /**
     * 删除文件夹内所有文件
     */
    private fun clearCacheDirectory(directory: File) {
        if (!directory.exists())
            return

        if (directory.isFile)
            directory.delete()

        directory.listFiles()?.forEach {
            if (it.isDirectory) {
                clearCacheDirectory(it)
            } else {
                it.delete()
            }
        }

    }

    /**
     * 获取文件夹内弹幕文件数量
     */
    private fun getDanmuFileCount(danmuDirectory: File): Int {
        if (!danmuDirectory.exists())
            return 0
        if (danmuDirectory.isFile && isDanmuFile(danmuDirectory.absolutePath))
            return 1

        var totalCount = 0
        danmuDirectory.listFiles()?.forEach {
            if (it.isDirectory) {
                totalCount += getDanmuFileCount(it)
            } else if (isDanmuFile(it.absolutePath)) {
                totalCount += 1
            }
        }

        return totalCount
    }

    /**
     * 获取文件夹内字幕文件数量
     */
    private fun getSubtitleFileCount(subtitleDirectory: File): Int {
        if (!subtitleDirectory.exists())
            return 0
        if (subtitleDirectory.isFile && isSubtitleFile(subtitleDirectory.absolutePath))
            return 1

        var totalCount = 0
        subtitleDirectory.listFiles()?.forEach {
            if (it.isDirectory) {
                totalCount += getSubtitleFileCount(it)
            } else if (isSubtitleFile(it.absolutePath)) {
                totalCount += 1
            }
        }

        return totalCount
    }
}