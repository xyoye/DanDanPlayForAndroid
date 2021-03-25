package com.xyoye.local_component.ui.activities.shooter_subtitle

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.SubDetailData
import com.xyoye.data_component.data.SubtitleSubData
import com.xyoye.local_component.utils.SearchSubtitleRepository
import kotlinx.coroutines.flow.*

class ShooterSubtitleViewModel : BaseViewModel() {

    private val searchSubtitleRepository = SearchSubtitleRepository(viewModelScope)
    val searchSubDetailLiveData = MutableLiveData<SubDetailData>()

    val searchSubtitleLiveData = searchSubtitleRepository.subtitleLiveData

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

    fun downloadSubtitle(fileName: String, downloadUrl: String) {
        httpRequest<String?>(viewModelScope) {
            onStart { showLoading() }

            api {
                val responseBody = Retrofit.extService.downloadResource(downloadUrl)
                SubtitleUtils.saveSubtitle(fileName, responseBody.byteStream())
            }

            onSuccess {
                if (it != null) {
                    ToastCenter.showSuccess("字幕下载成功：$it", Toast.LENGTH_LONG)
                } else {
                    ToastCenter.showError("保存字幕失败")
                }
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
                        ToastCenter.showSuccess("字幕下载成功：$it", Toast.LENGTH_LONG)
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