package com.xyoye.player_component.ui.activities.player

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.source.base.BaseVideoSource
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.FileHashUtils
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.comparator.FileNameComparator
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
            if (DanmuUtils.isDanmuContentEmpty(historyDanmuPath).not()) {
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

        var hash: String? = null
        try {
            val response = Retrofit.extService.downloadResource(videoSource.getVideoUrl(), headers)
            hash = FileHashUtils.getHash(response.byteStream())
        } catch (e: Exception) {
            e.printStackTrace()
        }

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

        httpRequest<List<DanmuSourceContentBean>>(viewModelScope) {

            api {
                val searchResult = Retrofit.service.searchDanmu(searchText, "")
                val animeData = searchResult.animes ?: mutableListOf()

                mapDanmuSourceData(animeData)
            }

            onSuccess {
                danmuSearchLiveData.postValue(it)
            }
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
        httpRequest<Pair<String, Int>?>(viewModelScope) {
            onStart { showLoading() }

            api {
                val danmuData = Retrofit.service.getDanmuContent(
                    contentBean.episodeId.toString(),
                    true
                )
                val danmuFileName = contentBean.animeTitle + "_" + contentBean.episodeTitle + ".xml"
                val danmuPath = DanmuUtils.saveDanmu(danmuData, null, danmuFileName)


                if (danmuPath.isNullOrEmpty()) {
                    null
                } else {
                    Pair(danmuPath, contentBean.episodeId)
                }
            }

            onSuccess {
                downloadDanmuLiveData.postValue(it)
            }

            onError {
                downloadDanmuLiveData.postValue(null)
            }

            onComplete { hideLoading() }
        }
    }
}