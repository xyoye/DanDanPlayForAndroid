package com.xyoye.common_component.network.repository

import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.storage.impl.RemoteStorage

/**
 * Created by xyoye on 2024/1/10.
 */

object RemoteRepository : BaseRepository() {

    /**
     * 获取远程媒体库所有文件
     */
    suspend fun getStorageFiles(storage: RemoteStorage) = request()
        .param("token", storage.library.remoteSecret)
        .doGet {
            Retrofit.remoteService.getStorageFiles(storage.rootUri.toString(), it)
        }

    /**
     * 下载视频相关弹幕
     */
    suspend fun downloadDanmu(storage: RemoteStorage, videoId: String) = request()
        .param("token", storage.library.remoteSecret)
        .doGet {
            Retrofit.remoteService.downloadDanmu(storage.rootUri.toString(), videoId, it)
        }

    /**
     * 获取视频相关字幕列表
     */
    suspend fun getRelatedSubtitles(storage: RemoteStorage, videoId: String) = request()
        .param("token", storage.library.remoteSecret)
        .doGet {
            Retrofit.remoteService.getRelatedSubtitles(storage.rootUri.toString(), videoId, it)
        }

    /**
     * 下载视频相关字幕
     */
    suspend fun downloadSubtitle(storage: RemoteStorage, videoId: String, fileName: String) = request()
        .param("token", storage.library.remoteSecret)
        .param("fileName", fileName)
        .doGet {
            Retrofit.remoteService.downloadSubtitle(storage.rootUri.toString(), videoId, it)
        }
}