package com.xyoye.player_component.ui.activities.player

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.extension.isValid
import com.xyoye.common_component.extension.toFile
import com.xyoye.common_component.network.repository.SourceRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.network.request.dataOrNull
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.FileHashUtils
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.comparator.FileNameComparator
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.DanmuSourceContentBean
import com.xyoye.data_component.bean.LoadDanmuBean
import com.xyoye.data_component.data.DanmuAnimeData
import com.xyoye.data_component.enums.LoadDanmuState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by xyoye on 2022/1/2.
 */

class PlayerDanmuViewModel : BaseViewModel() {
    val loadDanmuLiveData = MutableLiveData<LoadDanmuBean>()
    val danmuSearchLiveData = MutableLiveData<List<DanmuSourceContentBean>>()
    val downloadDanmuLiveData = MutableLiveData<Pair<String, Int>?>()

    fun loadDanmu(videoSource: BaseVideoSource) {
        val loadResult = LoadDanmuBean(videoSource.getVideoUrl())
        val historyDanmuPath = videoSource.getDanmuPath()

        viewModelScope.launch(Dispatchers.IO) {
            //如果弹幕内容不为空，则无需匹配弹幕
            if (historyDanmuPath.toFile().isValid()) {
                loadResult.state = LoadDanmuState.NO_MATCH_REQUIRE
                loadResult.danmuPath = historyDanmuPath
                loadResult.episodeId = videoSource.getEpisodeId()
                loadDanmuLiveData.postValue(loadResult)
                return@launch
            }

            //根据弹幕路径选择合适弹幕匹配方法
            val videoUrl = videoSource.getVideoUrl()
            val uri = Uri.parse(videoUrl)
            when (uri.scheme) {
                "http", "https" -> {
                    loadNetworkDanmu(videoSource)
                }

                "file", "content" -> {
                    loadLocalDanmu(videoUrl)
                }

                else -> {
                    //本地视频的绝对路径，例：/storage/emulate/0/Download/test.mp4
                    if (videoUrl.startsWith("/")) {
                        loadLocalDanmu(videoUrl)
                    } else {
                        loadDanmuLiveData.postValue(loadResult)
                    }
                }
            }
        }
    }

    private suspend fun loadLocalDanmu(videoUrl: String) {
        val loadResult = LoadDanmuBean(videoUrl)

        val uri = Uri.parse(videoUrl)
        val fileHash = IOUtils.getFileHash(uri.path)
        if (fileHash == null) {
            loadDanmuLiveData.postValue(loadResult)
            return
        }

        loadResult.state = LoadDanmuState.MATCHING
        loadDanmuLiveData.postValue(loadResult)
        val danmuInfo = DanmuUtils.matchDanmuSilence(videoUrl, fileHash)
        if (danmuInfo == null) {
            loadResult.state = LoadDanmuState.NO_MATCHED
            loadDanmuLiveData.postValue(loadResult)
            return
        }

        loadResult.state = LoadDanmuState.MATCH_SUCCESS
        loadResult.danmuPath = danmuInfo.first
        loadResult.episodeId = danmuInfo.second
        loadDanmuLiveData.postValue(loadResult)
    }

    private suspend fun loadNetworkDanmu(videoSource: BaseVideoSource) {
        val loadResult = LoadDanmuBean(videoSource.getVideoUrl())
        val headers = videoSource.getHttpHeader() ?: emptyMap()

        loadResult.state = LoadDanmuState.COLLECTING
        loadDanmuLiveData.postValue(loadResult)

        val hash = SourceRepository.getResourceResponseBody(videoSource.getVideoUrl(), headers)
            .dataOrNull
            ?.let { FileHashUtils.getHash(it.byteStream()) }

        if (hash.isNullOrEmpty()) {
            loadResult.state = LoadDanmuState.NOT_SUPPORTED
            loadDanmuLiveData.postValue(loadResult)
            return
        }

        loadResult.state = LoadDanmuState.MATCHING
        loadDanmuLiveData.postValue(loadResult)
        val danmuInfo = DanmuUtils.matchDanmuSilence(videoSource.getVideoTitle(), hash)
        if (danmuInfo == null) {
            loadResult.state = LoadDanmuState.NO_MATCHED
            loadDanmuLiveData.postValue(loadResult)
            return
        }

        loadResult.state = LoadDanmuState.MATCH_SUCCESS
        loadResult.danmuPath = danmuInfo.first
        loadResult.episodeId = danmuInfo.second
        loadDanmuLiveData.postValue(loadResult)
    }

    fun searchDanmu(searchText: String) {
        if (searchText.isEmpty())
            return

        viewModelScope.launch {
            val result = SourceRepository.searchDanmu(searchText)
            val animeData = result.dataOrNull?.animes ?: mutableListOf()
            val sourceData = mapDanmuSourceData(animeData)

            danmuSearchLiveData.postValue(sourceData)
        }
    }

    private fun mapDanmuSourceData(animeData: MutableList<DanmuAnimeData>): List<DanmuSourceContentBean> {
        val danmuData = mutableListOf<DanmuSourceContentBean>()

        animeData.sortedWith(FileNameComparator(
            getName = { it.animeTitle ?: "" },
            isDirectory = { false }
        )).forEach { anime ->
            val animeName = anime.animeTitle ?: return@forEach
            val episodes = anime.episodes ?: return@forEach

            val contentData = episodes.map {
                DanmuSourceContentBean(animeName, it.episodeTitle, it.episodeId)
            }
            danmuData.addAll(contentData)
        }

        return danmuData
    }

    fun downloadDanmu(contentBean: DanmuSourceContentBean) {
        viewModelScope.launch {
            showLoading()
            val result = SourceRepository.getDanmuContent(contentBean.episodeId.toString())

            if (result is Response.Error) {
                hideLoading()
                ToastCenter.showError(result.error.toastMsg)
                downloadDanmuLiveData.postValue(null)
                return@launch
            }

            val danmuFileName = contentBean.animeTitle + "_" + contentBean.episodeTitle + ".xml"
            val saveResult = result.dataOrNull
                ?.let { DanmuUtils.saveDanmu(it, null, danmuFileName) }
                ?.let { it to contentBean.episodeId }

            hideLoading()
            downloadDanmuLiveData.postValue(saveResult)
        }
    }
}