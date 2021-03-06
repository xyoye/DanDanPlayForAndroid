package com.xyoye.user_component.ui.fragment.personal

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.data_component.bean.UserRelationBean
import com.xyoye.data_component.data.CloudHistoryListData
import com.xyoye.data_component.data.FollowAnimeData

/**
 * Created by xyoye on 2020/7/28.
 */

class PersonalFragmentViewModel : BaseViewModel() {
    var followData: FollowAnimeData? = null
    var historyData: CloudHistoryListData? = null

    val relationLiveData = MutableLiveData<Pair<Int, Int>>()

    fun getUserRelationInfo() {
        httpRequest<Pair<Int, Int>>(viewModelScope) {
            api {
                followData = Retrofit.service.getFollowAnime()
                historyData = Retrofit.service.getCloudHistory()
                Pair(
                    followData?.favorites?.size ?: 0,
                    historyData?.playHistoryAnimes?.size ?: 0
                )
            }

            onSuccess {
                relationLiveData.postValue(it)
            }
        }
    }
}