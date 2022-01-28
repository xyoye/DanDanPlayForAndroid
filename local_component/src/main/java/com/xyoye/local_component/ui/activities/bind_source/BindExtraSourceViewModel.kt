package com.xyoye.local_component.ui.activities.bind_source

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Created by xyoye on 2022/1/24
 */
class BindExtraSourceViewModel : BaseViewModel() {

    val searchText = ObservableField<String>()
    val boundDanmu = ObservableBoolean()
    val boundSubtitle = ObservableBoolean()

    fun updateSourceChanged(uniqueKey: String, mediaType: MediaType) {
        viewModelScope.launch(Dispatchers.IO) {
            val history = DatabaseManager.instance.getPlayHistoryDao().getPlayHistory(uniqueKey, mediaType)
            boundDanmu.set(history?.danmuPath.isNullOrEmpty().not())
            boundSubtitle.set(history?.subtitlePath.isNullOrEmpty().not())
        }
    }
}