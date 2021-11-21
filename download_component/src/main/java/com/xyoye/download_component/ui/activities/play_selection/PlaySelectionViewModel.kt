package com.xyoye.download_component.ui.activities.play_selection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xunlei.downloadlib.parameter.*
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.media.TorrentMediaSource
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.thunder.ThunderManager
import com.xyoye.common_component.weight.ToastCenter
import kotlinx.coroutines.*

class PlaySelectionViewModel : BaseViewModel() {
    val torrentDownloadLiveData = MutableLiveData<String>()
    val dismissLiveData = MutableLiveData<Boolean>()
    val playLiveData = MutableLiveData<Any>()

    fun downloadTorrentFile(magnetLink: String) {
        viewModelScope.launch {
            showLoading()
            val torrentFilePath = ThunderManager.getInstance().downloadTorrentFile(
                magnetLink,
                PathHelper.getDownloadTorrentDirectory()
            )
            hideLoading()

            if (torrentFilePath.isNullOrEmpty()) {
                ToastCenter.showError("种子文件下载失败，请重试")
                dismissLiveData.postValue(true)
                return@launch
            }

            torrentDownloadLiveData.postValue(torrentFilePath)
        }
    }

    fun torrentPlay(torrentPath: String, selectIndex: Int) {
        viewModelScope.launch {
            showLoading()
            val mediaSource = TorrentMediaSource.build(selectIndex, torrentPath)
            hideLoading()

            if (mediaSource == null) {
                ToastCenter.showError("启动播放任务失败，请重试")
                dismissLiveData.postValue(true)
                return@launch
            }

            VideoSourceManager.getInstance().setSource(mediaSource)
            playLiveData.postValue(Any())
        }
    }
}