package com.xyoye.common_component.network.repository

import com.xyoye.common_component.network.Retrofit

/**
 * Created by xyoye on 2024/1/6.
 */

object UserRepository : BaseRepository() {

    /**
     * 账号登录
     */
    suspend fun login(
        account: String,
        password: String,
        appId: String,
        timestamp: String,
        sign: String
    ) = request()
        .param("userName", account)
        .param("password", password)
        .param("appId", appId)
        .param("unixTimestamp", timestamp)
        .param("hash", sign)
        .doPost {
            Retrofit.danDanService.login(it)
        }


    /**
     * 刷新Token
     */
    suspend fun refreshToken() = request()
        .doGet {
            Retrofit.danDanService.refreshToken()
        }

    /**
     * 注册账号
     */
    suspend fun register(
        account: String,
        password: String,
        screenName: String,
        email: String,
        appId: String,
        timestamp: String,
        sign: String
    ) = request()
        .param("userName", account)
        .param("password", password)
        .param("screenName", screenName)
        .param("email", email)
        .param("appId", appId)
        .param("unixTimestamp", timestamp)
        .param("hash", sign)
        .doPost {
            Retrofit.danDanService.register(it)
        }

    /**
     * 重置密码
     */
    suspend fun resetPassword(
        account: String,
        email: String,
        appId: String,
        timestamp: String,
        sign: String
    ) = request()
        .param("userName", account)
        .param("email", email)
        .param("appId", appId)
        .param("unixTimestamp", timestamp)
        .param("hash", sign)
        .doPost {
            Retrofit.danDanService.resetPassword(it)
        }

    /**
     * 找回账号
     */
    suspend fun retrieveAccount(
        email: String,
        appId: String,
        timestamp: String,
        sign: String
    ) = request()
        .param("email", email)
        .param("appId", appId)
        .param("unixTimestamp", timestamp)
        .param("hash", sign)
        .doPost {
            Retrofit.danDanService.retrieveAccount(it)
        }

    /**
     * 修改昵称
     */
    suspend fun updateScreenName(screenName: String) = request()
        .param("screenName", screenName)
        .doPost {
            Retrofit.danDanService.updateScreenName(it)
        }

    /**
     * 修改密码
     */
    suspend fun updatePassword(oldPassword: String, newPassword: String) = request()
        .param("oldPassword", oldPassword)
        .param("newPassword", newPassword)
        .doPost {
            Retrofit.danDanService.updatePassword(it)
        }
}