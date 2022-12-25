package com.xyoye.player_component.ui.activities.player_intent

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.common_component.utils.SupervisorScope
import com.xyoye.common_component.utils.getDirPath
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.entity.ExtendFolderEntity
import com.xyoye.data_component.enums.MediaType
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
            val extendVideos = MediaUtils.scanVideoFile(folderPath)
            if (extendVideos.isNotEmpty()) {
                DatabaseManager.instance.getExtendFolderDao().insert(
                    ExtendFolderEntity(folderPath, extendVideos.size)
                )
            }
        }
    }

    fun openIntentUrl(url: String) {
        viewModelScope.launch {
            showLoading()
            val mediaSource = VideoSourceFactory.Builder()
                .setVideoSources(listOf(url))
                .create(MediaType.OTHER_STORAGE)
            hideLoading()

            if (mediaSource == null) {
                isParseError.set(true)
                ToastCenter.showError("播放失败，无法打开播放资源")
                return@launch
            }
            VideoSourceManager.getInstance().setSource(mediaSource)
            playLiveData.postValue(Any())
        }
    }
}
