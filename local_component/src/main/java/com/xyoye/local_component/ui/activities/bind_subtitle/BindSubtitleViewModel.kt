package com.xyoye.local_component.ui.activities.bind_subtitle

import androidx.lifecycle.*
import androidx.paging.*
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.utils.getFileNameNoExtension
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.*
import com.xyoye.local_component.utils.SearchSubtitleRepository
import com.xyoye.local_component.utils.SubtitleHashUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BindSubtitleViewModel : BaseViewModel() {

    private val searchSubtitleRepository = SearchSubtitleRepository(viewModelScope)

    val sourceLiveData = MutableLiveData<MutableList<SubtitleMatchData>>()
    val bindResultLiveData = MutableLiveData<Boolean>()
    val unzipResultLiveData = MutableLiveData<String>()
    val searchSubDetailLiveData = MutableLiveData<SubDetailData>()

    val searchSubtitleLiveData = searchSubtitleRepository.subtitleLiveData

    /**
     * 匹配字幕
     */
    fun matchSubtitleSource(videoPath: String) {
        httpRequest<MutableList<SubtitleMatchData>>(viewModelScope) {
            onStart { showLoading() }

            api {
                mutableListOf<SubtitleMatchData>().apply {
                    addAll(matchThunderSubtitle(videoPath))
                    addAll(matchShooterSubtitle(videoPath))
                }
            }

            onSuccess {
                if (it.isEmpty()) {
                    ToastCenter.showError("未匹配到相关字幕，请尝试搜索字幕")
                    return@onSuccess
                }
                sourceLiveData.postValue(it)
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }
    }

    /**
     * 匹配迅雷字幕
     */
    private suspend fun matchThunderSubtitle(videoPath: String): MutableList<SubtitleMatchData> {
        val subtitleList = mutableListOf<SubtitleMatchData>()

        val videoHash = SubtitleHashUtils.getThunderHash(videoPath) ?: return subtitleList

        val thunderUrl = "http://sub.xmp.sandai.net:8000/subxl/$videoHash.json"

        var subtitleData: SubtitleThunderData? = null
        try {
            subtitleData = Retrofit.extService.matchThunderSubtitle(thunderUrl)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (subtitleData?.sublist?.size ?: 0 > 0) {
            for (thunderData in subtitleData!!.sublist!!) {
                //下载链接不能为空
                if (thunderData.surl == null)
                    continue

                //评分转换
                var rate = 0
                try {
                    rate = thunderData.rate?.toInt() ?: 0
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }

                subtitleList.add(
                    SubtitleMatchData(
                        thunderData.sname ?: "",
                        thunderData.surl!!,
                        "迅雷",
                        rate
                    )
                )
            }
        }

        return subtitleList
    }

    /**
     * 匹配射手字幕
     */
    private suspend fun matchShooterSubtitle(videoPath: String): MutableList<SubtitleMatchData> {
        val subtitleList = mutableListOf<SubtitleMatchData>()

        val videoHash = SubtitleHashUtils.getShooterHash(videoPath) ?: return subtitleList

        val shooterParams = HashMap<String, String>()
        shooterParams["filehash"] = videoHash
        shooterParams["pathinfo"] = getFileName(videoPath)
        shooterParams["format"] = "json"
        shooterParams["lang"] = "Chn"

        val shooterUrl = "https://www.shooter.cn/api/subapi.php"

        var shooterSubtitleList: MutableList<SubtitleShooterData>? = null
        try {
            shooterSubtitleList =
                Retrofit.extService.matchShooterSubtitle(shooterUrl, shooterParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        if (shooterSubtitleList != null) {
            for (subtitleData in shooterSubtitleList) {
                if (subtitleData.Files == null)
                    continue
                for (shooterData in subtitleData.Files!!) {
                    //下载链接不能为空
                    if (shooterData.Link == null)
                        continue

                    val extension = shooterData.Ext ?: ".ass"
                    val shooterName = getFileNameNoExtension(videoPath) + "." + extension
                    subtitleList.add(
                        SubtitleMatchData(
                            shooterName,
                            shooterData.Link!!,
                            "射手网",
                            -1
                        )
                    )
                }
            }
        }

        return subtitleList
    }

    /**
     * 下载迅雷、射手字幕
     */
    fun downloadSubtitle(videoPath: String, fileName: String, downloadUrl: String) {
        httpRequest<Boolean>(viewModelScope) {
            onStart { showLoading() }

            api {
                val responseBody = Retrofit.extService.downloadResource(downloadUrl)
                val subtitlePath = SubtitleUtils.saveSubtitle(fileName, responseBody.byteStream())
                if (subtitlePath != null) {
                    bindLocalSubtitle(videoPath, subtitlePath)
                    true
                } else {
                    false
                }
            }

            onSuccess {
                bindResultLiveData.postValue(it)
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }
    }

    /**
     * 将字幕路径与视频绑定
     */
    fun bindLocalSubtitle(videoPath: String, subtitlePath: String) {
        viewModelScope.launch {
            DatabaseManager.instance
                .getVideoDao()
                .updateSubtitle(videoPath, subtitlePath)
        }
    }

    /**
     * 搜索字幕
     */
    fun searchSubtitle(videoName: String) {
        searchSubtitleRepository.search(videoName)
    }

    /**
     * 获取搜索字幕详情
     */
    fun getSearchSubDetail(subtitleId: Int) {
        val shooterSecret = SubtitleConfig.getShooterSecret() ?: ""
        httpRequest<SubtitleSubData>(viewModelScope) {
            onStart { showLoading() }

            api { Retrofit.extService.searchSubtitleDetail(shooterSecret, subtitleId.toString()) }

            onSuccess {
                if (it.sub?.subs == null || it.sub!!.subs!!.size == 0) {
                    ToastCenter.showError("获取字幕详情失败")
                    return@onSuccess
                }
                searchSubDetailLiveData.postValue(it.sub!!.subs!![0])
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }
    }

    /**
     * 下载压缩文件，并解压
     */
    fun downloadAndUnzipFile(fileName: String, url: String) {
        httpRequest<Any>(viewModelScope) {
            onStart { showLoading() }

            api {
                val responseBody = Retrofit.extService.downloadResource(url)
                //这里用回调处理不是很好，暂时没有更好方案
                SubtitleUtils.saveAndUnzipFile(fileName, responseBody.byteStream()) {
                    if (it.isNotEmpty()) {
                        unzipResultLiveData.postValue(it)
                    } else {
                        ToastCenter.showError("解压字幕文件失败，请尝试手动解压")
                    }
                }
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }
    }
}