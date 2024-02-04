package com.xyoye.player_component.ui.activities.player_intent

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.factory.StorageVideoSourceFactory
import com.xyoye.common_component.storage.StorageFactory
import com.xyoye.common_component.storage.impl.LinkStorage
import com.xyoye.common_component.utils.SupervisorScope
import com.xyoye.common_component.utils.getDirPath
import com.xyoye.common_component.utils.meida.VideoScan
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.ExtendFolderEntity
import com.xyoye.data_component.entity.MediaLibraryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PlayerIntentViewModel : BaseViewModel() {
    val playLiveData = MutableLiveData<Any>()

    val isParseError = ObservableField(false)

    /**
     * 未识别的视频，将其目录添加至扩展扫描目录
     */
    fun addUnrecognizedFile(filePath: String) {
        SupervisorScope.IO.launch {
            val videoFile = File(filePath)
            if (videoFile.exists().not())
                return@launch

            val videoData = DatabaseManager.instance.getVideoDao().findVideoByPath(filePath)
            if (videoData != null)
                return@launch

            val folderPath = getDirPath(filePath)
            val extendVideos = VideoScan.traverse(folderPath)
            if (extendVideos.isNotEmpty()) {
                DatabaseManager.instance.getExtendFolderDao().insert(
                    ExtendFolderEntity(folderPath, extendVideos.size)
                )
            }
        }
    }

    fun openIntentUrl(link: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (setupLinkSource(link)) {
                playLiveData.postValue(Any())
            }
        }
    }

    private suspend fun setupLinkSource(link: String): Boolean {
        showLoading()
        val mediaSource = MediaLibraryEntity.HISTORY.copy(url = link)
            .run { StorageFactory.createStorage(this) }
            ?.run { this as? LinkStorage }
            ?.run { getRootFile() }
            ?.run { StorageVideoSourceFactory.create(this) }
        hideLoading()

        if (mediaSource == null) {
            ToastCenter.showError("播放失败，找不到播放资源")
            return false
        }
        VideoSourceManager.getInstance().setSource(mediaSource)
        return true
    }
}
