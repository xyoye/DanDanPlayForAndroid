package com.xyoye.local_component.ui.fragment.bind_danmu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.toFile
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.repository.DanDanPlayRepository
import com.xyoye.common_component.network.request.data
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.DanmuUtils
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.common_component.utils.comparator.FileNameComparator
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


/**
 * Created by xyoye on 2022/1/25
 */
class BindDanmuSourceFragmentViewModel : BaseViewModel() {

    lateinit var storageFile: StorageFile

    val danmuHeaderLiveData = MutableLiveData<List<DanmuSourceHeaderBean>>()
    val danmuContentLiveData = MutableLiveData<List<DanmuSourceContentBean>>()
    val thirdSourceLiveData = MutableLiveData<Pair<DanmuSourceContentBean, DanmuRelatedData>>()
    val sourceRefreshLiveData = MutableLiveData<Any>()

    private val curAnimeList = mutableListOf<DanmuSourceHeaderBean>()
    private var danmuMatchBean: DanmuSourceHeaderBean? = null
    private var history: PlayHistoryEntity? = null
    private var currentTab: Int = 0

    fun matchDanmu() {
        if (storageFile.storage.library.mediaType != MediaType.LOCAL_STORAGE) {
            return
        }
        val videoFile = storageFile.filePath().toFile()
        httpRequest<Unit>(viewModelScope) {
            onStart { showLoading() }

            api {
                history = DatabaseManager.instance.getPlayHistoryDao().getPlayHistory(
                    storageFile.uniqueKey(), storageFile.storage.library.id
                )

                val fileHash = IOUtils.getFileHash(videoFile!!.absolutePath) ?: ""
                val result = DanDanPlayRepository.matchDanmu(fileHash)
                danmuMatchBean = mapDanmuMatchData(result.data)
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
                if (danmuMatchBean != null) {
                    animeList.add(0, danmuMatchBean!!)
                }

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
        httpRequest(viewModelScope) {
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
        httpRequest(viewModelScope) {
            onStart { showLoading() }

            api {
                val danmuData = Retrofit.service.getDanmuContent(
                    contentBean.episodeId.toString(),
                    true
                )
                val danmuPath = DanmuUtils.saveDanmu(
                    danmuData,
                    contentBean.animeTitle,
                    "${contentBean.episodeTitle}.xml"
                )
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

        httpRequest(viewModelScope) {
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


                val danmuPath = DanmuUtils.saveDanmu(
                    allDanmuData,
                    contentBean.animeTitle,
                    "${contentBean.episodeTitle}.xml"
                )
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
            sourceRefreshLiveData.postValue(Any())
        }
    }

    fun bindLocalDanmu(filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseDanmu(filePath, 0)
            sourceRefreshLiveData.postValue(Any())
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
    ): MutableList<DanmuSourceHeaderBean> {
        val danmuData = mutableListOf<DanmuSourceHeaderBean>()

        animeData.sortedWith(FileNameComparator(
            getName = { it.animeTitle ?: "" },
            isDirectory = { false }
        )).forEach { anime ->
            val animeName = anime.animeTitle ?: return@forEach
            val episodes = anime.episodes ?: return@forEach

            val contentData = episodes.map {
                DanmuSourceContentBean(
                    animeName,
                    it.episodeTitle,
                    it.episodeId,
                    false
                )
            }

            danmuData.add(
                DanmuSourceHeaderBean(
                    anime.animeId,
                    animeName,
                    contentData,
                    false
                )
            )
        }

        return danmuData
    }

    private fun mapDanmuMatchData(
        matchData: DanmuMatchData?
    ): DanmuSourceHeaderBean? {
        if (matchData == null || matchData.success.not() || matchData.isMatched.not()) {
            return null
        }

        val danmuContents = matchData.matches?.map {
            DanmuSourceContentBean(
                it.animeTitle ?: "",
                it.episodeTitle ?: "",
                it.episodeId,
                isRecommend = true
            )
        } ?: return null

        return DanmuSourceHeaderBean(
            0,
            "推荐弹幕",
            danmuContents,
            isRecommend = true
        )
    }

    private suspend fun databaseDanmu(
        danmuPath: String?,
        episodeId: Int
    ) {
        val storageId = storageFile.storage.library.id
        val history = DatabaseManager.instance.getPlayHistoryDao()
            .getPlayHistory(storageFile.uniqueKey(), storageId)
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
            mediaType = storageFile.storage.library.mediaType,
            uniqueKey = storageFile.uniqueKey(),
            danmuPath = danmuPath,
            episodeId = episodeId,
            storageId = storageId,
        )
        this.history = newHistory
        DatabaseManager.instance.getPlayHistoryDao().insert(newHistory)
        selectTab(currentTab)
    }
}