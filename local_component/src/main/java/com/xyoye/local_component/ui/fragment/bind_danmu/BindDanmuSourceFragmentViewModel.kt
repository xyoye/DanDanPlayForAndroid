package com.xyoye.local_component.ui.fragment.bind_danmu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.toFile
import com.xyoye.common_component.network.repository.ResourceRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.network.request.dataOrNull
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
                val result = ResourceRepository.matchDanmu(fileHash)
                danmuMatchBean = mapDanmuMatchData(result.dataOrNull)
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

        viewModelScope.launch {
            showLoading()
            val result = ResourceRepository.searchDanmu(searchText)
            hideLoading()

            val animeData = result.dataOrNull?.animes ?: mutableListOf()
            val animeList = mapDanmuAnimeData(animeData)
            if (danmuMatchBean != null) {
                animeList.add(0, danmuMatchBean!!)
            }

            curAnimeList.clear()
            curAnimeList.addAll(animeList)

            selectTab(0)
        }
    }

    fun getDanmuThirdSource(contentBean: DanmuSourceContentBean) {
        viewModelScope.launch {
            showLoading()
            val result = ResourceRepository.getRelatedDanmu(contentBean.episodeId.toString())
            hideLoading()

            if (result is Response.Error) {
                ToastCenter.showError(result.error.toastMsg)
                return@launch
            }

            if (result is Response.Success) {
                thirdSourceLiveData.postValue(contentBean to result.data)
            }
        }
    }

    fun downloadDanmu(contentBean: DanmuSourceContentBean) {
        viewModelScope.launch {
            showLoading()
            val result = ResourceRepository.getDanmuContent(contentBean.episodeId.toString())
            hideLoading()

            if(result is Response.Error) {
                ToastCenter.showError(result.error.toastMsg)
                return@launch
            }

            if (result is Response.Success) {
                val danmuPath = DanmuUtils.saveDanmu(
                    result.data,
                    contentBean.animeTitle,
                    "${contentBean.episodeTitle}.xml"
                )
                if (danmuPath.isNullOrEmpty()) {
                    ToastCenter.showError("保存弹幕失败")
                } else {
                    ToastCenter.showSuccess("保存弹幕成功！")
                    databaseDanmu(danmuPath, contentBean.episodeId)
                }

                sourceRefreshLiveData.postValue(Any())
            }

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

        viewModelScope.launch {
            val allDanmuData = DanmuData(0)
            showLoading()
            for (source in danmuSources) {
                val result = if (source.isOfficial) {
                    ResourceRepository.getDanmuContent(source.sourceUrl, withRelated = false)
                } else {
                    ResourceRepository.getRelatedDanmuContent(source.sourceUrl)
                }

                if (result is Response.Error) {
                    hideLoading()
                    ToastCenter.showError(result.error.toastMsg)
                    return@launch
                }

                result.dataOrNull?.let {
                    allDanmuData.count += it.count
                    allDanmuData.comments.addAll(it.comments)
                }
            }
            hideLoading()

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
            sourceRefreshLiveData.postValue(Any())
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