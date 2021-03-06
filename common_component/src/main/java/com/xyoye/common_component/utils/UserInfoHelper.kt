package com.xyoye.common_component.utils

import androidx.lifecycle.MutableLiveData
import com.xyoye.common_component.config.UserConfig
import com.xyoye.data_component.data.LoginData

/**
 * Created by xyoye on 2021/1/11.
 */

object UserInfoHelper {

    val loginLiveData = MutableLiveData<LoginData>()
    var mLoginData: LoginData? = null

    fun login(loginData: LoginData): Boolean {
        val userToken = loginData.token
        if (!userToken.isNullOrEmpty()) {
            mLoginData = loginData
            UserConfig.putUserToken(userToken)
            UserConfig.putUserLoggedIn(true)
            updateLoginInfo()
            return true
        }
        exitLogin()
        return false
    }

    fun exitLogin() {
        mLoginData = null
        UserConfig.putUserToken("")
        UserConfig.putUserLoggedIn(false)
        updateLoginInfo()
    }

    fun updateLoginInfo(){
        loginLiveData.postValue(mLoginData)
    }
}