package com.xyoye.user_component.ui.fragment.scan_extend

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.ExtendFolderEntity
import kotlinx.coroutines.launch

class ScanExtendFragmentViewModel : BaseViewModel() {

    val extendFolderLiveData = MutableLiveData<MutableList<Any>>()

    fun getExtendFolder() {
        viewModelScope.launch {
            val entities = DatabaseManager.instance.getExtendFolderDao().getAll()
            val extendFolderList = arrayListOf<Any>()
            //扩展目录
            extendFolderList.addAll(entities)
            //添加按钮
            extendFolderList.add(0)
            extendFolderLiveData.postValue(extendFolderList)
        }
    }

    fun removeExtendFolder(entity: ExtendFolderEntity) {
        viewModelScope.launch {
            DatabaseManager.instance.getExtendFolderDao().delete(entity.folderPath)
            DatabaseManager.instance.getVideoDao().deleteExtend()
            getExtendFolder()
        }
    }

    fun addExtendFolder(folderPath: String) {
        viewModelScope.launch {
            showLoading()

            val extendVideos = MediaUtils.scanVideoFile(folderPath)
            if (extendVideos.size == 0){
                ToastCenter.showError("失败，未识别到任何视频")
                hideLoading()
                return@launch
            }

            val entity = ExtendFolderEntity(folderPath, extendVideos.size)
            DatabaseManager.instance.getExtendFolderDao().insert(entity)
            getExtendFolder()
            hideLoading()
        }
    }
}