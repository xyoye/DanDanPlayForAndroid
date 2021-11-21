package com.xyoye.player_component.ui.activities.player_intent

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.getDirPath
import com.xyoye.data_component.entity.ExtendFolderEntity
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

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

    /**
     * 未识别的视频，将其目录添加至扩展扫描目录
     */
    fun addUnrecognizedFile(filePath: String) {
        GlobalScope.launch(Dispatchers.IO) {
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
}