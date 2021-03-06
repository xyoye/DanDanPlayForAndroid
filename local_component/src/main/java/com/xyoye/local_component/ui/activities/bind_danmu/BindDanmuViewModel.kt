package com.xyoye.local_component.ui.activities.bind_danmu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.DanmuSourceBean
import com.xyoye.data_component.data.DanmuData
import com.xyoye.data_component.data.DanmuMatchData
import com.xyoye.data_component.data.DanmuMatchDetailData
import com.xyoye.data_component.data.DanmuRelatedData
import kotlinx.coroutines.launch
import java.io.File

class BindDanmuViewModel : BaseViewModel() {

    val relatedLiveData = MutableLiveData<Pair<DanmuMatchDetailData, DanmuRelatedData>>()
    val sourceLiveData = MutableLiveData<MutableList<DanmuMatchDetailData>>()
    val bindSuccessLiveData = MutableLiveData<Pair<String, Int>>()

    fun getDanmuSource(videoPath: String) {
        httpRequest<DanmuMatchData>(viewModelScope) {
            onStart { showLoading() }

            api {
                val params = HashMap<String, String>()
                params["fileName"] = getFileName(videoPath)
                params["fileHash"] = IOUtils.getFileHash(videoPath) ?: ""
                params["fileSize"] = File(videoPath).length().toString()
                params["videoDuration"] = "0"
                params["matchMode"] = "hashOnly"

                Retrofit.service.matchDanmu(params)
            }

            onSuccess {
                if (it.matches == null) {
                    ToastCenter.showError("未匹配到相关弹幕，请尝试搜索弹幕")
                    return@onSuccess
                }
                sourceLiveData.postValue(it.matches)
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }
    }

    fun getDanmuRelated(matchBean: DanmuMatchDetailData) {
        httpRequest<DanmuRelatedData>(viewModelScope) {
            onStart { showLoading() }

            api {
                Retrofit.service.getDanmuRelated(matchBean.episodeId.toString())
            }

            onSuccess {
                relatedLiveData.postValue(Pair(matchBean, it))
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }
    }

    fun searchDanmuSource(animeName: String, episodeId: String) {
        httpRequest<MutableList<DanmuMatchDetailData>>(viewModelScope) {
            onStart { showLoading() }

            api {
                val fakeMatchData = mutableListOf<DanmuMatchDetailData>()

                val danmuSearchData = Retrofit.service.searchDanmu(animeName, episodeId)

                //遍历转换为展示对象
                danmuSearchData.animes?.let { animeList ->
                    for (anime in animeList) {
                        anime.episodes?.let { episodeList ->
                            for (episodeData in episodeList) {
                                fakeMatchData.add(
                                    DanmuMatchDetailData(
                                        episodeData.episodeId,
                                        anime.animeId,
                                        anime.animeTitle,
                                        episodeData.episodeTitle,
                                        anime.type,
                                        0
                                    )
                                )
                            }
                        }
                    }
                }

                fakeMatchData
            }

            onSuccess {
                sourceLiveData.postValue(it)
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }
    }

    fun downloadDanmu(
        danmuSources: MutableList<DanmuSourceBean>,
        isCheckedAll: Boolean,
        videoPath: String?,
        episodeId: Int,
        danmuFileName: String
    ) {
        if (danmuSources.size == 0) {
            ToastCenter.showWarning("请至少选择一个弹幕源")
            return
        }

        httpRequest<Pair<String, Int>?>(viewModelScope) {
            onStart { showLoading() }

            api {
                var allDanmuData = DanmuData(0)

                if (isCheckedAll) {
                    //选中所有，通过指定弹幕库下载，并开启合并第三方源
                    allDanmuData = Retrofit.service.getDanmuContent(
                        danmuSources[0].sourceUrl,
                        true,
                        danmuSources[0].format
                    )
                } else {
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
                }

                //保存弹幕
                val folderName = videoPath?.run { getParentFolderName(videoPath) }
                //视频路径存在（即本地视频），将弹幕文件重命名为视频文件名，否则按弹幕标题命名
                val fileName = if (videoPath == null) danmuFileName else "${getFileExtension(videoPath)}.xml"
                val danmuPath = DanmuUtils.saveDanmu(allDanmuData, folderName, fileName)

                if (danmuPath.isNullOrEmpty()) {
                    ToastCenter.showError("保存弹幕失败")
                } else {
                    if (videoPath != null) {
                        DatabaseManager.instance
                            .getVideoDao()
                            .updateDanmu(videoPath, danmuPath, episodeId)
                        ToastCenter.showSuccess("绑定弹幕成功！")
                    }
                }

                if (danmuPath != null){
                    return@api Pair(danmuPath, episodeId)
                } else {
                    return@api null
                }
            }

            onSuccess { danmuData ->
                danmuData?.let {
                    bindSuccessLiveData.postValue(danmuData)
                }
            }

            onError {
                ToastCenter.showError("x${it.code} ${it.msg}")
            }

            onComplete { hideLoading() }
        }
    }

    fun bindLocalDanmu(videoPath: String?, danmuPath: String) {
        viewModelScope.launch {
            if (videoPath != null) {
                DatabaseManager.instance
                    .getVideoDao()
                    .updateDanmu(videoPath, danmuPath)
                ToastCenter.showSuccess("绑定弹幕成功！")
            }
            bindSuccessLiveData.postValue(Pair(danmuPath, 0))
        }
    }
}