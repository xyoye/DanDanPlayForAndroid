package com.xyoye.user_component.ui.activities.user_info

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.UserRepository
import com.xyoye.common_component.utils.UserInfoHelper
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.LoginData
import kotlinx.coroutines.launch

class UserInfoViewModel : BaseViewModel() {
    val userAccountField = ObservableField<String>()
    val userScreenNameField = ObservableField<String>()

    val updatePasswordLiveData = MutableLiveData<String>()
    val updateScreenNameLiveData = MutableLiveData<String>()

    fun applyLoginData(loginData: LoginData) {
        userAccountField.set(loginData.userName)
        userScreenNameField.set(loginData.screenName)
    }

    fun updateScreenName(screenName: String) {
        viewModelScope.launch {
            showLoading()
            val result = UserRepository.updateScreenName(screenName)
            hideLoading()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            UserInfoHelper.mLoginData?.screenName = screenName
            UserInfoHelper.updateLoginInfo()
            updateScreenNameLiveData.postValue(screenName)
            ToastCenter.showSuccess("修改昵称成功")
        }
    }

    fun updatePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            showLoading()
            val result = UserRepository.updatePassword(oldPassword, newPassword)
            hideLoading()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            val userAccount = UserInfoHelper.mLoginData?.userName.orEmpty()
            UserInfoHelper.exitLogin()
            updatePasswordLiveData.postValue(userAccount)
            ToastCenter.showSuccess("修改密码成功")
        }
    }
}