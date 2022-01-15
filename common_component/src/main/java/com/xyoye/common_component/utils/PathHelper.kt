package com.xyoye.common_component.utils

import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.DefaultConfig
import java.io.File

/**
 * Created by xyoye on 2020/12/29.
 */

object PathHelper {

    const val PATH_DANMU = "danmu"
    const val PATH_SUBTITLE = "subtitle"
    const val PATH_PLAY_CACHE = "play_cache"
    const val PATH_SCREEN_SHOT = "screen_shot"
    const val PATH_VIDEO_COVER = "video_cover"
    const val PATH_EXO_CACHE = ".exo_cache"

    private const val PATH_DOWNLOAD = "download/files"
    private const val PATH_DOWNLOAD_RESUME = "download/.resume"
    private const val PATH_DOWNLOAD_TORRENT = "download/torrent"

    /**
     * 私有目录路径
     */
    const val PRIVATE_DIRECTORY_PATH = "/Android/data/com.xyoye.dandanplay/"

    /**
     * 获取缓存路径
     */
    fun getCachePath() = AppConfig.getCachePath() ?: DefaultConfig.DEFAULT_CACHE_PATH

    /**
     * 获取保存弹幕的文件夹
     */
    fun getDanmuDirectory(): File {
        return File(getCachePath(), PATH_DANMU).apply {
            checkDirectory(this)
        }
    }

    /**
     * 获取保存字幕的文件夹
     */
    fun getSubtitleDirectory(): File {
        return File(getCachePath(), PATH_SUBTITLE).apply {
            checkDirectory(this)
        }
    }

    /**
     * 获取视频封面的文件夹
     */
    fun getVideoCoverDirectory(): File {
        return File(getCachePath(), PATH_VIDEO_COVER).apply {
            checkDirectory(this)
        }
    }


    /**
     * 获取下载文件目录
     */
    fun getDownloadDirectory(): File {
        return File(getCachePath(), PATH_DOWNLOAD).apply {
            checkDirectory(this)
        }
    }

    /**
     * 获取下载种子目录
     */
    fun getDownloadTorrentDirectory(): File {
        return File(getCachePath(), PATH_DOWNLOAD_TORRENT).apply {
            checkDirectory(this)
        }
    }

    /**
     * 获取下载恢复文件的文件夹
     */
    fun getDownloadResumeDirectory(): File {
        return File(getCachePath(), PATH_DOWNLOAD_RESUME).apply {
            checkDirectory(this)
        }
    }

    /**
     * 获取播放的临时缓存文件夹
     */
    fun getPlayCacheDirectory(): File {
        return File(getCachePath(), PATH_PLAY_CACHE).apply {
            checkDirectory(this)
        }
    }

    /**
     * 获取播放截图的缓存文件夹
     */
    fun getScreenShotDirectory(): File {
        return File(getCachePath(), PATH_SCREEN_SHOT).apply {
            checkDirectory(this)
        }
    }

    /**
     * 获取播放截图的缓存文件夹
     */
    fun getExoCacheDirectory(): File {
        return File(getCachePath(), PATH_EXO_CACHE).apply {
            checkDirectory(this)
        }
    }

    /**
     * 获取下载配置文件
     */
    fun getDownloadSettingsFile(): File {
        return File(getDownloadResumeDirectory(), ".download_settings.dat").apply {
            checkFile(this)
        }
    }

    fun resumeDataFile(infoHash: String): File {
        return File(getDownloadResumeDirectory(), "$infoHash.resume")
    }

    fun torrentFile(name: String): File {
        return File(getDownloadTorrentDirectory(), "$name.torrent")
    }

    fun resumeTorrentFile(name: String): File {
        return File(getDownloadResumeDirectory(), "$name.torrent")
    }

    private fun checkDirectory(dirFile: File) {
        if (dirFile.isFile) {
            dirFile.delete()
        }
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
    }

    private fun checkFile(file: File) {
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

}