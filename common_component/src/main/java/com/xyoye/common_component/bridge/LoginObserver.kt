package com.xyoye.common_component.bridge

import androidx.lifecycle.MutableLiveData
import com.xyoye.data_component.data.LoginData

/**
 * Created by xyoye on 2021/1/9.
 */

interface LoginObserver {
    fun getLoginLiveData(): MutableLiveData<LoginData>
}