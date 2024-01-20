package com.xyoye.common_component.network.repository

import com.xyoye.common_component.network.Retrofit

/**
 * Created by xyoye on 2024/1/20.
 */

object AlistRepository : BaseRepository() {

    /**
     * 登录Alist，获取Token
     */
    suspend fun login(url: String, userName: String, password: String) = request()
        .param("username", userName)
        .param("password", password)
        .doPost {
            Retrofit.alistService.login(url, it)
        }

    /**
     * 获取Alist根目录
     */
    suspend fun getRootFile(url: String, token: String) = request()
        .doGet {
            Retrofit.alistService.getRootPath(url, token)
        }

    /**
     * 打开文件夹
     */
    suspend fun openDirectory(url: String, token: String, path: String) = request()
        .param("path", path)
        .doPost {
            Retrofit.alistService.openDirectory(url, token, it)
        }

    /**
     * 打开文件夹
     */
    suspend fun openFile(url: String, token: String, path: String) = request()
        .param("path", path)
        .doPost {
            Retrofit.alistService.openFile(url, token, it)
        }
}