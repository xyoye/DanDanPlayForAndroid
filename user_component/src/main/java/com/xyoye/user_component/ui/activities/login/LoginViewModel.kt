package com.xyoye.user_component.ui.activities.login

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.repository.UserRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.utils.SecurityHelper
import com.xyoye.common_component.utils.UserInfoHelper
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.LoginData
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel() {

    val accountField = ObservableField("")
    val passwordField = ObservableField("")

    val accountErrorLiveData = MutableLiveData<String>()
    val passwordErrorLiveData = MutableLiveData<String>()
    val loginLiveData = MutableLiveData<LoginData>()

    fun login() {

        val account = accountField.get()
        val password = passwordField.get()

        val allowLogin = checkAccount(account) && checkPassword(password)
        if (!allowLogin)
            return

        val appId = SecurityHelper.getInstance().appId
        val unixTimestamp = System.currentTimeMillis() / 1000
        val hashInfo = appId + password + unixTimestamp + account
        val hash = SecurityHelper.getInstance().buildHash(hashInfo)

        viewModelScope.launch {
            showLoading()
            val result = UserRepository.login(
                account!!,
                password!!,
                appId,
                unixTimestamp.toString(),
                hash
            )
            hideLoading()

            if (result is Response.Error) {
                ToastCenter.showError(result.error.toastMsg)
                return@launch
            }

            if (result is Response.Success) {
                if (UserInfoHelper.login(result.data)) {
                    ToastCenter.showSuccess("登录成功")
                    loginLiveData.postValue(result.data)
                } else {
                    ToastCenter.showError("登录错误，请稍后再试")
                }
            }
        }

    }

    private fun checkAccount(account: String?): Boolean {
        if (account.isNullOrEmpty()) {
            accountErrorLiveData.postValue("请输入帐号")
            return false
        }
        return true
    }

    private fun checkPassword(password: String?): Boolean {
        if (password.isNullOrEmpty()) {
            passwordErrorLiveData.postValue("请输入密码")
            return false
        }
        return true
    }
}