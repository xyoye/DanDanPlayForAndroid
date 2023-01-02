package com.xyoye.stream_component.ui.fragment.storage_file

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.storage.Storage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.FileComparator
import com.xyoye.common_component.utils.isVideoFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StorageFileFragmentViewModel : BaseViewModel() {
    private val hidePointFile = AppConfig.isShowHiddenFile().not()

    private val _fileLiveData = MutableLiveData<List<StorageFile>>()
    val fileLiveData = _fileLiveData

    fun listFile(storage: Storage, directory: StorageFile?) {
        viewModelScope.launch(Dispatchers.IO) {
            val target = directory ?: storage.getRootFile()
            if (target == null) {
                _fileLiveData.postValue(emptyList())
                return@launch
            }

            val childFiles = storage.openDirectory(target)
                .filter {
                    isDisplayFile(it)
                }.sortedWith(
                    FileComparator({ it.fileName() }, { it.isDirectory() })
                )
            _fileLiveData.postValue(childFiles)
        }
    }

    /**
     * 是否可展示的文件
     */
    private fun isDisplayFile(storageFile: StorageFile): Boolean {
        //.开头的文件，根据配置展示
        if (hidePointFile && storageFile.fileName().startsWith(".")) {
            return false
        }
        //文件夹，展示
        if (storageFile.isDirectory()) {
            return true
        }
        //视频文件，展示
        return isVideoFile(storageFile.fileName())
    }
}