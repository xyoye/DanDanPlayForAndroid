package com.xyoye.user_component.ui.fragment.scan_extend

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.storage.StorageFactory
import com.xyoye.common_component.utils.meida.VideoScan
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.ExtendFolderEntity
import com.xyoye.data_component.entity.MediaLibraryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScanExtendFragmentViewModel : BaseViewModel() {

    val extendFolderLiveData = MutableLiveData<MutableList<Any>>()
    val extendAppendedLiveData = MutableLiveData<Any>()

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

            // 移除本地视频库中关联的视频
            DatabaseManager.instance.getVideoDao().deleteExtend()
            // 刷新扩展目录UI
            getExtendFolder()
        }
    }

    fun addExtendFolder(folderPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading()
            val extendVideos = VideoScan.traverse(folderPath)
            hideLoading()

            if (extendVideos.isEmpty()) {
                ToastCenter.showError("失败，当前文件夹内未识别到任何视频")
                return@launch
            }

            // 新增扩展目录到数据库
            val entity = ExtendFolderEntity(folderPath, extendVideos.size)
            DatabaseManager.instance.getExtendFolderDao().insert(entity)

            // 刷新本地视频库
            refreshVideoStorage()
            // 刷新扩展目录UI
            getExtendFolder()

            // 关闭弹窗
            extendAppendedLiveData.postValue(Any())
        }
    }

    /**
     * 刷新本地视频库
     */
    private fun refreshVideoStorage() {
        viewModelScope.launch {
            val storage = StorageFactory.createStorage(MediaLibraryEntity.LOCAL) ?: return@launch
            val rootFile = storage.getRootFile() ?: return@launch
            storage.openDirectory(rootFile, true)
        }
    }
}