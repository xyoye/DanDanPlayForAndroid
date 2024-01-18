package com.xyoye.player_component.ui.activities.player

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.utils.danmu.DanmuFinder
import com.xyoye.common_component.utils.danmu.source.DanmuSourceFactory
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.LoadDanmuResult
import com.xyoye.data_component.bean.LocalDanmuBean
import com.xyoye.data_component.data.DanmuEpisodeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by xyoye on 2022/1/2.
 */

class PlayerDanmuViewModel : BaseViewModel() {
    val loadDanmuLiveData = MutableLiveData<LoadDanmuResult>()
    val danmuSearchLiveData = MutableLiveData<List<DanmuEpisodeData>>()
    val downloadDanmuLiveData = MutableLiveData<LocalDanmuBean>()

    fun loadDanmu(videoSource: BaseVideoSource) {
        viewModelScope.launch(Dispatchers.IO) {
            // 如果视频已经存在弹幕，直接加载
            val historyDanmuPath = videoSource.getDanmuPath()
            if (historyDanmuPath?.isNotEmpty() == true) {
                val loadResult = LoadDanmuResult(
                    videoSource.getVideoUrl(),
                    historyDanmuPath,
                    videoSource.getEpisodeId(),
                    isHistoryData = true
                )
                loadDanmuLiveData.postValue(loadResult)
                return@launch
            }

            // 如果视频不存在弹幕，尝试匹配弹幕
            DanmuSourceFactory.build(videoSource)
                ?.let {
                    DanmuFinder.instance.downloadMatched(it)
                }?.let {
                    LoadDanmuResult(videoSource.getVideoUrl(), it.danmuPath, it.episodeId)
                }?.let {
                    loadDanmuLiveData.postValue(it)
                }
        }
    }

    fun searchDanmu(searchText: String) {
        if (searchText.isEmpty())
            return

        viewModelScope.launch {
            val result = DanmuFinder.instance.search(searchText).flatMap { it.episodes }
            danmuSearchLiveData.postValue(result)
        }
    }

    fun downloadDanmu(episode: DanmuEpisodeData) {
        viewModelScope.launch {
            showLoading()
            val result = DanmuFinder.instance.downloadEpisode(episode)
            hideLoading()

            if (result == null) {
                ToastCenter.showError("弹幕保存失败")
                return@launch
            }

            downloadDanmuLiveData.postValue(result)
        }
    }
}