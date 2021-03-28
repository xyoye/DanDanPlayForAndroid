package com.xyoye.local_component.ui.fragment.media

import android.provider.MediaStore
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.utils.getFileName
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by xyoye on 2020/7/27.
 */

class MediaViewModel : BaseViewModel() {

    val mediaLibLiveData = DatabaseManager.instance.getMediaLibraryDao().getAll()

    fun initLocalStorage() {
        val localStorageEntity = MediaLibraryEntity(
            1,
            "本地媒体库",
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString(),
            MediaType.LOCAL_STORAGE
        )
        val streamEntity = MediaLibraryEntity(
            2,
            "串流播放",
            "url://dandanplay_steam_link",
            MediaType.STREAM_LINK,
            null,
            null,
            true,
            0,
            "https://"
        )
        val magnetEntity = MediaLibraryEntity(
            3,
            "磁链播放",
            "url://dandanplay_magnet_link",
            MediaType.MAGNET_LINK,
            null,
            null,
            true,
            0,
            "magnet:?xt=urn:btih:"
        )
        val historyEntity = MediaLibraryEntity(
            4,
            "播放历史",
            "",
            MediaType.OTHER_STORAGE
        )
        viewModelScope.launch(context = Dispatchers.IO) {
            //播放历史首条记录
            DatabaseManager.instance.getPlayHistoryDao().gitLastPlay(
                MediaType.LOCAL_STORAGE,
                MediaType.OTHER_STORAGE,
                MediaType.FTP_SERVER,
                MediaType.SMB_SERVER,
                MediaType.REMOTE_STORAGE,
                MediaType.WEBDAV_SERVER
            )?.apply {
                historyEntity.url = url
            }

            //磁链播放首条记录
            DatabaseManager.instance.getPlayHistoryDao().gitLastPlay(MediaType.MAGNET_LINK)?.apply {
                magnetEntity.describe = getFileName(torrentPath)
            }

            //串流播放首条记录
            DatabaseManager.instance.getPlayHistoryDao().gitLastPlay(MediaType.STREAM_LINK)?.apply {
                streamEntity.describe = url
            }

            DatabaseManager.instance.getMediaLibraryDao()
                .insert(localStorageEntity, streamEntity, magnetEntity, historyEntity)
        }
    }

    fun deleteStorage(data: MediaLibraryEntity) {
        viewModelScope.launch(context = Dispatchers.IO) {
            DatabaseManager.instance.getMediaLibraryDao()
                .delete(data.url, data.mediaType)
        }
    }
}