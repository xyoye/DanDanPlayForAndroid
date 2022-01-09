package com.xyoye.player_component.ui.activities.player_intent

import androidx.databinding.ObservableField
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.utils.MediaUtils
import com.xyoye.common_component.utils.getDirPath
import com.xyoye.data_component.entity.ExtendFolderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class PlayerIntentViewModel : BaseViewModel() {
    val isParseError = ObservableField(false)

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