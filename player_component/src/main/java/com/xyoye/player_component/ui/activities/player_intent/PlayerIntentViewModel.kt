package com.xyoye.player_component.ui.activities.player_intent

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerIntentViewModel : BaseViewModel() {
    val isParseError = ObservableField(false)

    val historyLiveData = MutableLiveData<PlayHistoryEntity?>()

    fun queryHistory(videoUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading()
            val entity = PlayHistoryUtils.getPlayHistory(videoUrl, MediaType.OTHER_STORAGE)
            hideLoading()
            historyLiveData.postValue(entity)
        }
    }
}