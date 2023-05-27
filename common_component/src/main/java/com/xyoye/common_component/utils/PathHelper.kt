package com.xyoye.common_component.utils

import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.DefaultConfig
import com.xyoye.data_component.enums.CacheType
import java.io.File

/**
 * Created by xyoye on 2020/12/29.
 */

object PathHelper {

    //OpenCC文件夹名
    private const val OPEN_CC_DIRECTORY = "open_cc"

    /**
     * 获取缓存路径
     */
    fun getCachePath() = AppConfig.getCachePath() ?: DefaultConfig.DEFAULT_CACHE_PATH

    /**
     * 获取保存弹幕的文件夹
     */
    fun getDanmuDirectory(): File {
        return getCacheDirectory(CacheType.DANMU_CACHE)
    }

    /**
     * 获取保存字幕的文件夹
     */
    fun getSubtitleDirectory(): File {
        return getCacheDirectory(CacheType.SUBTITLE_CACHE)
    }

    /**
     * 获取视频封面的文件夹
     */
    fun getVideoCoverDirectory(): File {
        return getCacheDirectory(CacheType.VIDEO_COVER_CACHE)
    }

    /**
     * 获取下载种子目录
     */
    fun getTorrentDirectory(): File {
        return getCacheDirectory(CacheType.TORRENT_FILE_CACHE)
    }

    /**
     * 获取播放的临时缓存文件夹
     */
    fun getPlayCacheDirectory(): File {
        return getCacheDirectory(CacheType.PLAY_CACHE)
    }

    /**
     * 获取播放截图的缓存文件夹
     */
    fun getScreenShotDirectory(): File {
        return getCacheDirectory(CacheType.SCREEN_SHOT_CACHE)
    }

    /**
     * 获取缓存文件文件夹
     */
    fun getCacheDirectory(type: CacheType): File {
        return File(getCachePath(), type.dirName).apply {
            checkDirectory(this)
        }
    }

    /**
     * 获取OpenCC配置文件文件夹
     */
    fun getOpenCCDirectory(): File {
        val fileDir = BaseApplication.getAppContext().filesDir
        return File(fileDir, OPEN_CC_DIRECTORY).apply {
            checkDirectory(this)
        }
    }

    private fun checkDirectory(dirFile: File) {
        if (dirFile.isFile) {
            dirFile.delete()
        }
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
    }

}