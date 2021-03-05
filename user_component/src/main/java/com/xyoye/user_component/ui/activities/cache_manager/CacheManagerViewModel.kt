package com.xyoye.user_component.ui.activities.cache_manager

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.utils.*
import com.xyoye.data_component.enums.CacheType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CacheManagerViewModel : BaseViewModel() {

    //系统缓存
    private val systemCacheDir = BaseApplication.getAppContext().cacheDir
    val systemCachePath = ObservableField("")
    val systemCacheSizeText = ObservableField("")

    //文件缓存总大小
    private val externalCacheFile = File(PathHelper.getCachePath())
    var externalCachePath = ObservableField("")
    var externalCacheSizeText = ObservableField("")

    //弹幕缓存
    private val danmuDirectory = PathHelper.getDanmuDirectory()
    var danmuFileCount = ObservableField("")
    var danmuDirectoryName = ObservableField("")
    var danmuDirectorySizeText = ObservableField("")

    //字幕缓存
    private val subtitleDirectory = PathHelper.getSubtitleDirectory()
    var subtitleFileCount = ObservableField("")
    var subtitleDirectorySizeText = ObservableField("")
    var subtitleDirectoryName = ObservableField("")

    //播放器缓存
    private val playCacheDirectory = PathHelper.getPlayCacheDirectory()
    private val exoCacheDirectory = PathHelper.getPlayCacheDirectory()
    var playerDirectoryCacheSizeText = ObservableField("")
    var playerCacheDirectoryName = ObservableField("")

    //截图缓存
    private val screenShotDirectory = PathHelper.getScreenShotDirectory()
    var screenShotDirectorySizeText = ObservableField("")
    var screenShotDirectoryName = ObservableField("")

    //其它缓存
    var otherCacheSizeText = ObservableField("")

    val confirmCacheLiveData = MutableLiveData<CacheType>()

    init {
        getCacheSize()
    }

    fun clearCache(type: CacheType) {
        confirmCacheLiveData.postValue(type)
    }

    fun confirmClearCache(type: CacheType) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (type) {
                    CacheType.SYSTEM_CACHE -> clearCacheDirectory(systemCacheDir)
                    CacheType.DANMU_CACHE -> clearCacheDirectory(danmuDirectory)
                    CacheType.SUBTITLE_CACHE -> clearCacheDirectory(subtitleDirectory)
                    CacheType.PLAY_CACHE -> {
                        clearCacheDirectory(playCacheDirectory)
                        clearCacheDirectory(exoCacheDirectory)
                    }
                    CacheType.SCREEN_SHOT_CACHE -> clearCacheDirectory(screenShotDirectory)
                    CacheType.OTHER_CACHE -> {
                        val cacheDir = arrayOf(
                            danmuDirectory.absolutePath,
                            subtitleDirectory.absolutePath,
                            playCacheDirectory.absolutePath,
                            exoCacheDirectory.absolutePath,
                            screenShotDirectory.absolutePath
                        )
                        exoCacheDirectory.listFiles()?.forEach {
                            if (it.absolutePath !in cacheDir) {
                                clearCacheDirectory(it)
                            }
                        }
                    }
                    else -> {
                    }
                }
                getCacheSize()
            }
        }
    }

    private fun getCacheSize() {
        //系统缓存
        val systemCacheSize = IOUtils.getDirectorySize(systemCacheDir)
        systemCachePath.set("路径：${systemCacheDir.absolutePath}")
        systemCacheSizeText.set(formatFileSize(systemCacheSize))

        //文件缓存总大小
        val externalCacheSize = IOUtils.getDirectorySize(externalCacheFile)
        externalCachePath.set("路径：${externalCacheFile.absolutePath}")
        externalCacheSizeText.set(formatFileSize(externalCacheSize))

        //弹幕缓存
        val danmuDirectorySize = IOUtils.getDirectorySize(danmuDirectory)
        danmuFileCount.set("弹幕文件（${getDanmuFileCount(danmuDirectory)}）")
        danmuDirectoryName.set("文件夹名称：${PathHelper.PATH_DANMU}")
        danmuDirectorySizeText.set(formatFileSize(IOUtils.getDirectorySize(danmuDirectory)))

        //字幕缓存
        val subtitleDirectorySize = IOUtils.getDirectorySize(subtitleDirectory)
        subtitleFileCount.set("字幕文件（${getSubtitleFileCount(subtitleDirectory)}）")
        subtitleDirectorySizeText.set(formatFileSize(IOUtils.getDirectorySize(subtitleDirectory)))
        subtitleDirectoryName.set("文件夹名称：${PathHelper.PATH_SUBTITLE}")

        //播放器缓存
        val exoCacheDirectorySize = IOUtils.getDirectorySize(exoCacheDirectory)
        val playCacheDirectorySize = IOUtils.getDirectorySize(playCacheDirectory)
        playerDirectoryCacheSizeText.set(
            formatFileSize(playCacheDirectorySize + exoCacheDirectorySize)
        )
        playerCacheDirectoryName.set("文件夹名称：${PathHelper.PATH_PLAY_CACHE}")

        //截图缓存
        val screenShotDirectorySize = IOUtils.getDirectorySize(screenShotDirectory)
        screenShotDirectorySizeText.set(formatFileSize(screenShotDirectorySize))
        screenShotDirectoryName.set("文件夹名称：${PathHelper.PATH_SCREEN_SHOT}")

        val otherCacheSize = externalCacheSize - danmuDirectorySize - subtitleDirectorySize -
                playCacheDirectorySize - exoCacheDirectorySize - screenShotDirectorySize
        otherCacheSizeText.set(formatFileSize(otherCacheSize))
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