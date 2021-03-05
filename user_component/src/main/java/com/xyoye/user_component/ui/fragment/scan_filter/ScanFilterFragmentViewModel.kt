package com.xyoye.user_component.ui.fragment.scan_filter

import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import kotlinx.coroutines.launch

class ScanFilterFragmentViewModel : BaseViewModel() {
    val folderLiveData = DatabaseManager.instance.getVideoDao().getAllFolder()

    fun updateFolder(folderPath: String, filter: Boolean){
        viewModelScope.launch {
            DatabaseManager.instance.getVideoDao().updateFolderFilter(filter, folderPath)
        }
    }
}