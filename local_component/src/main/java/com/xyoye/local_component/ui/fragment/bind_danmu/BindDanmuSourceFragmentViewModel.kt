package com.xyoye.local_component.ui.fragment.bind_danmu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.DanmuSourceBean
import com.xyoye.data_component.bean.DanmuSourceContentBean
import com.xyoye.data_component.bean.DanmuSourceHeaderBean
import com.xyoye.data_component.data.DanmuAnimeData
import com.xyoye.data_component.data.DanmuData
import com.xyoye.data_component.data.DanmuMatchData
import com.xyoye.data_component.data.DanmuRelatedData
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


/**
 * Created by xyoye on 2022/1/25
 */
class BindDanmuSourceFragmentViewModel : BaseViewModel() {

    lateinit var uniqueKey: String
    lateinit var mediaType: MediaType
    var videoPath: String? = null

    val danmuHeaderLiveData = MutableLiveData<List<DanmuSourceHeaderBean>>()
    val danmuContentLiveData = MutableLiveData<List<DanmuSourceContentBean>>()
    val thirdSourceLiveData = MutableLiveData<Pair<DanmuSourceContentBean, DanmuRelatedData>>()
    val sourceRefreshLiveData = MutableLiveData<Any>()

    private val curAnimeList = mutableListOf<DanmuSourceHeaderBean>()
    private var danmuMatchBean: DanmuSourceHeaderBean? = null
    private var history: PlayHistoryEntity? = null
    private var currentTab: Int = 0

    fun matchDanmu(videoPath: String) {
        httpRequest<Unit>(viewModelScope) {
            onStart { showLoading() }

            api {
                val params = HashMap<String, String>()
                params["fileName"] = getFileName(videoPath)
                params["fileHash"] = IOUtils.getFileHash(videoPath) ?: ""
                params["fileSize"] = File(videoPath).length().toString()
                params["videoDuration"] = "0"
                params["matchMode"] = "hashOnly"

                history = DatabaseManager.instance.getPlayHistoryDao().getPlayHistory(
                    uniqueKey, mediaType
                )

                val matchData = Retrofit.service.matchDanmu(params)
                danmuMatchBean = mapDanmuMatchData(matchData)
                danmuMatchBean?.let { curAnimeList.add(it) }
            }

            onSuccess {
                selectTab(0)
            }

            onComplete { hideLoading() }
        }
    }

    fun searchDanmu(searchText: String) {
        if (searchText.isEmpty())
            return

        httpRequest<Unit>(viewModelScope) {
            onStart {
                showLoading()
            }

            api {
                val searchResult = Retrofit.service.searchDanmu(searchText, "")
                val animeData = searchResult.animes ?: mutableListOf()

                val animeList = mapDanmuAnimeData(animeData)

                curAnimeList.clear()
                curAnimeList.addAll(animeList)
            }

            onSuccess {
                selectTab(0)
            }

            onComplete { hideLoading() }
        }
    }

    fun getDanmuThirdSource(contentBean: DanmuSourceContentBean) {
        httpRequest<DanmuRelatedData>(viewModelScope) {
            onStart { showLoading() }

            api {
                Retrofit.service.getDanmuRelated(contentBean.episodeId.toString())
            }

            onSuccess {
                thirdSourceLiveData.postValue(Pair(contentBean, it))
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }
    }

    fun downloadDanmu(contentBean: DanmuSourceContentBean) {
        httpRequest<Unit>(viewModelScope) {
            onStart { showLoading() }

            api {
                val language = DanmuConfig.getDefaultLanguage()
                val danmuData = Retrofit.service.getDanmuContent(
                    contentBean.episodeId.toString(),
                    true,
                    language
                )
                val danmuFileName = contentBean.animeTitle + "_" + contentBean.episodeTitle + ".xml"
                val danmuPath = saveDanmuFile(videoPath, danmuFileName, danmuData)
                if (danmuPath.isNullOrEmpty()) {
                    ToastCenter.showError("保存弹幕失败")
                } else {
                    ToastCenter.showSuccess("保存弹幕成功！")
                    databaseDanmu(danmuPath, contentBean.episodeId)
                }
            }

            onSuccess {
                sourceRefreshLiveData.postValue(Any())
            }

            onError {
                ToastCenter.showError("x${it.code} ${it.msg}")
            }

            onComplete { hideLoading() }
        }
    }

    fun downloadDanmu(
        danmuSources: MutableList<DanmuSourceBean>,
        isCheckedAll: Boolean,
        contentBean: DanmuSourceContentBean,
    ) {
        if (isCheckedAll) {
            downloadDanmu(contentBean)
            return
        }

        if (danmuSources.size == 0) {
            ToastCenter.showWarning("请至少选择一个弹幕源")
            return
        }

        httpRequest<Unit>(viewModelScope) {
            onStart { showLoading() }

            api {
                val allDanmuData = DanmuData(0)
                //下载并合并所有第三方源弹幕
                for (source in danmuSources) {
                    val danmuData = if (source.isOfficial) {
                        Retrofit.service.getDanmuContent(source.sourceUrl, false, source.format)
                    } else {
                        Retrofit.service.getDanmuExtContent(source.sourceUrl)
                    }
                    allDanmuData.count += danmuData.count
                    allDanmuData.comments.addAll(danmuData.comments)
                }

                val danmuFileName = contentBean.animeTitle + "_" + contentBean.episodeTitle + ".xml"
                val danmuPath = saveDanmuFile(videoPath, danmuFileName, allDanmuData)
                if (danmuPath.isNullOrEmpty()) {
                    ToastCenter.showError("保存弹幕失败")
                } else {
                    ToastCenter.showSuccess("保存弹幕成功！")
                    databaseDanmu(danmuPath, contentBean.episodeId)
                }
            }

            onSuccess {
                sourceRefreshLiveData.postValue(Any())
            }

            onError {
                ToastCenter.showError("x${it.code} ${it.msg}")
            }

            onComplete { hideLoading() }
        }
    }

    fun unbindDanmu() {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDanmu(null, 0)
        }
    }

    fun bindLocalDanmu(filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDanmu(filePath, 0)
        }
    }

    fun selectTab(tabIndex: Int) {
        currentTab = tabIndex
        val episodeData = mutableListOf<DanmuSourceContentBean>()
        for ((index, anime) in curAnimeList.withIndex()) {
            anime.isSelected = tabIndex == index
            anime.isLoaded = false

            anime.episodeData.forEach {
                if (it.episodeId != 0 && it.episodeId == history?.episodeId) {
                    it.isLoaded = true
                    anime.isLoaded = true
                } else {
                    it.isLoaded = false
                }
            }

            if (tabIndex == index) {
                episodeData.clear()
                episodeData.addAll(anime.episodeData)
            }
        }

        danmuHeaderLiveData.postValue(curAnimeList)
        danmuContentLiveData.postValue(episodeData)
    }

    private fun mapDanmuAnimeData(
        animeData: MutableList<DanmuAnimeData>
    ): List<DanmuSourceHeaderBean> {
        val danmuData = mutableListOf<DanmuSourceHeaderBean>()

        animeData.sortedWith(FileComparator(
            value = { it.animeTitle ?: "" },
            isDirectory = { false }
        )).forEach { anime ->
            val animeName = anime.animeTitle ?: return@forEach
            val episodes = anime.episodes ?: return@forEach

            val matchEpisode = danmuMatchBean?.episodeData?.getOrNull(0)

            val contentData = episodes.map {
                DanmuSourceContentBean(
                    animeName,
                    it.episodeTitle,
                    it.episodeId,
                    isRecommend = it.episodeId != 0 && it.episodeId == matchEpisode?.episodeId
                )
            }

            danmuData.add(
                DanmuSourceHeaderBean(
                    anime.animeId,
                    animeName,
                    contentData,
                    isRecommend = anime.animeId != 0 && anime.animeId == danmuMatchBean?.animeId
                )
            )
        }

        return danmuData
    }

    private fun mapDanmuMatchData(
        matchData: DanmuMatchData?
    ): DanmuSourceHeaderBean? {
        matchData ?: return null

        if (matchData.success && matchData.isMatched) {
            matchData.matches?.getOrNull(0)?.let {
                val contentBean = DanmuSourceContentBean(
                    it.animeTitle ?: "",
                    it.episodeTitle ?: "",
                    it.episodeId,
                    isRecommend = true
                )
                return DanmuSourceHeaderBean(
                    it.animeId,
                    it.animeTitle ?: "",
                    arrayListOf(contentBean),
                    isRecommend = true
                )
            }
        }
        return null
    }

    private suspend fun saveDanmuFile(
        videoPath: String?,
        danmuFileName: String,
        danmuData: DanmuData
    ): String? {
        return withContext(Dispatchers.IO) {
            //保存弹幕
            val folderName = videoPath?.run { getParentFolderName(videoPath) }
            //视频路径存在（即本地视频），将弹幕文件重命名为视频文件名，否则按弹幕标题命名
            val fileName =
                if (videoPath == null) danmuFileName else "${getFileNameNoExtension(videoPath)}.xml"
            return@withContext DanmuUtils.saveDanmu(danmuData, folderName, fileName)
        }
    }

    private suspend fun databaseDanmu(
        danmuPath: String?,
        episodeId: Int
    ) {
        val history = DatabaseManager.instance.getPlayHistoryDao()
            .getPlayHistory(uniqueKey, mediaType)
        if (history != null) {
            history.danmuPath = danmuPath
            history.episodeId = episodeId
            DatabaseManager.instance.getPlayHistoryDao().insert(history)
            this.history = history
            selectTab(currentTab)
            return
        }

        val newHistory = PlayHistoryEntity(
            0,
            "",
            "",
            mediaType,
            uniqueKey = uniqueKey,
            danmuPath = danmuPath,
            episodeId = episodeId
        )
        this.history = newHistory
        DatabaseManager.instance.getPlayHistoryDao().insert(newHistory)
        selectTab(currentTab)
    }
}