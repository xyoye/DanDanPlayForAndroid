package com.xyoye.user_component.ui.activities.forgot

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.repository.UserRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.utils.SecurityHelper
import com.xyoye.common_component.weight.ToastCenter
import kotlinx.coroutines.launch

class ForgotViewModel : BaseViewModel() {

    val isForgotPassword = ObservableField<Boolean>()

    val accountField = ObservableField("")
    val emailField = ObservableField("")

    val accountErrorLiveData = MutableLiveData<String>()
    val emailErrorLiveData = MutableLiveData<String>()
    val requestLiveData = MutableLiveData<Boolean>()

    fun confirm() {
        if (isForgotPassword.get() == true)
            resetPassword()
        else
            retrieveAccount()
    }

    private fun resetPassword() {
        val account = accountField.get()
        val email = emailField.get()

        val allowReset = checkAccount(account) && checkEmail(email)
        if (!allowReset)
            return

        val appId = SecurityHelper.getInstance().appId
        val unixTimestamp = System.currentTimeMillis() / 1000
        val hashInfo = appId + email + unixTimestamp + account
        val hash = SecurityHelper.getInstance().buildHash(hashInfo)

        viewModelScope.launch {
            showLoading()
            val result = UserRepository.resetPassword(
                account!!,
                email!!,
                appId,
                unixTimestamp.toString(),
                hash
            )
            hideLoading()

            if (result is Response.Error) {
                ToastCenter.showError(result.error.toastMsg)
                return@launch
            }

            ToastCenter.showSuccess("重置成功，密码已发送至邮箱")
            requestLiveData.postValue(true)
        }
    }

    private fun retrieveAccount() {
        val email = emailField.get()

        val allowRetrieve = checkEmail(email)
        if (!allowRetrieve)
            return

        val appId = SecurityHelper.getInstance().appId
        val unixTimestamp = System.currentTimeMillis() / 1000
        val hashInfo = appId + email + unixTimestamp
        val hash = SecurityHelper.getInstance().buildHash(hashInfo)

        viewModelScope.launch {
            showLoading()
            val result = UserRepository.retrieveAccount(
                email!!,
                appId,
                unixTimestamp.toString(),
                hash
            )
            hideLoading()

            if (result is Response.Error) {
                ToastCenter.showError(result.error.toastMsg)
                return@launch
            }

            ToastCenter.showSuccess("验证成功，帐号已发送至邮箱")
            requestLiveData.postValue(true)
        }
    }

    private fun checkAccount(account: String?): Boolean {
        if (account.isNullOrEmpty()) {
            accountErrorLiveData.postValue("请输入帐号")
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
}