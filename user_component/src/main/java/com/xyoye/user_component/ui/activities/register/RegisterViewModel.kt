package com.xyoye.user_component.ui.activities.register

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.SecurityHelper
import com.xyoye.common_component.utils.UserInfoHelper
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.LoginData

class RegisterViewModel : BaseViewModel() {

    val accountField = ObservableField<String>("")
    val emailField = ObservableField<String>("")
    val passwordField = ObservableField<String>("")
    val screenNameField = ObservableField<String>("")

    val accountErrorLiveData = MutableLiveData<String>()
    val passwordErrorLiveData = MutableLiveData<String>()
    val emailErrorLiveData = MutableLiveData<String>()
    val screenNameErrorLiveData = MutableLiveData<String>()

    val registerLiveData = MutableLiveData<LoginData>()

    fun register() {
        val account = accountField.get()
        val email = emailField.get()
        val password = passwordField.get()
        val screenName = screenNameField.get()

        val allowRegister = checkAccount(account)
                && checkPassword(password)
                && checkEmail(email)
                && checkScreenName(screenName)
        if (!allowRegister)
            return

        httpRequest<LoginData>(viewModelScope) {
            onStart { showLoading() }

            api {
                val appId = SecurityHelper.getInstance().appId
                val unixTimestamp = System.currentTimeMillis() / 1000
                val hashInfo = appId + email + password + screenName + unixTimestamp + account
                val hash = SecurityHelper.getInstance().buildHash(hashInfo)

                val params = HashMap<String, String>()
                params["appId"] = appId
                params["userName"] = account!!
                params["password"] = password!!
                params["email"] = email!!
                params["screenName"] = screenName!!
                params["unixTimestamp"] = unixTimestamp.toString()
                params["hash"] = hash

                Retrofit.service.register(params)
            }

            onSuccess {
                if (UserInfoHelper.login(it)){
                    ToastCenter.showSuccess("注册成功")
                    registerLiveData.postValue(it)
                } else {
                    ToastCenter.showError("注册错误，请稍后再试")
                }
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }

    }

    private fun checkAccount(account: String?): Boolean {
        if (account.isNullOrEmpty()) {
            accountErrorLiveData.postValue("请输入帐号")
            return false
        }
        if (account.length < 5) {
            accountErrorLiveData.postValue("帐号长度需为5-20")
            return false
        }
        if (!account.matches("^[a-zA-Z0-9]+$".toRegex())) {
            accountErrorLiveData.postValue("帐号仅支持英文和数字")
            return false
        }
        if (account.first().isDigit()) {
            accountErrorLiveData.postValue("帐号首位不能为数字")
            return false
        }
        return true
    }

    private fun checkPassword(password: String?): Boolean {
        if (password.isNullOrEmpty()) {
            passwordErrorLiveData.postValue("请输入密码")
            return false
        }
        if (password.length < 5) {
            passwordErrorLiveData.postValue("密码长度需为5-20")
            return false
        }
        return true
    }

    private fun checkEmail(email: String?): Boolean {
        if (email.isNullOrEmpty()) {
            emailErrorLiveData.postValue("请输入邮箱")
            return false
        }
        return true
    }

    private fun checkScreenName(screenName: String?): Boolean {
        if (screenName.isNullOrEmpty()) {
            screenNameErrorLiveData.postValue("请输入昵称")
            return false
        }
        return true
    }
}