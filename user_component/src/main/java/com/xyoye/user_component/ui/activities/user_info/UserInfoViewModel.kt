package com.xyoye.user_component.ui.activities.user_info

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.UserInfoHelper
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.CommonJsonData
import com.xyoye.data_component.data.LoginData

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
        httpRequest<CommonJsonData>(viewModelScope) {
            onStart { showLoading() }

            api {
                val params = HashMap<String, String>()
                params["screenName"] = screenName
                Retrofit.service.updateScreenName(params)
            }

            onSuccess {
                UserInfoHelper.mLoginData?.screenName = screenName
                UserInfoHelper.updateLoginInfo()
                updateScreenNameLiveData.postValue(screenName)
                ToastCenter.showSuccess("修改昵称成功")
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }
    }

    fun updatePassword(oldPassword: String, newPassword: String) {
        httpRequest<CommonJsonData>(viewModelScope) {
            onStart { showLoading() }

            api {
                val params = HashMap<String, String>()
                params["oldPassword"] = oldPassword
                params["newPassword"] = newPassword
                Retrofit.service.updatePassword(params)
            }

            onSuccess {
                val userAccount: String? = UserInfoHelper.mLoginData?.userName
                UserInfoHelper.exitLogin()
                updatePasswordLiveData.postValue(userAccount)
                ToastCenter.showSuccess("修改密码成功")
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }
    }
}