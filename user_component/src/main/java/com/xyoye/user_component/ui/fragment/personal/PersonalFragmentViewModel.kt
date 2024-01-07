package com.xyoye.user_component.ui.fragment.personal

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.common_component.network.request.dataOrNull
import com.xyoye.data_component.data.CloudHistoryListData
import com.xyoye.data_component.data.FollowAnimeData
import kotlinx.coroutines.launch

/**
 * Created by xyoye on 2020/7/28.
 */

class PersonalFragmentViewModel : BaseViewModel() {
    var followData: FollowAnimeData? = null
    var historyData: CloudHistoryListData? = null

    val relationLiveData = MutableLiveData<Pair<Int, Int>>()

    fun getUserRelationInfo() {
        viewModelScope.launch {
            val followedSize = AnimeRepository.getFollowedAnime()
                .dataOrNull?.favorites?.size ?: 0
            val historySize = AnimeRepository.getPlayHistory()
                .dataOrNull?.playHistoryAnimes?.size ?: 0

            relationLiveData.postValue(followedSize to historySize)
        }
    }
}