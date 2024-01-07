package com.xyoye.local_component.ui.activities.shooter_subtitle

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.repository.SourceRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.network.request.dataOrNull
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.subtitle.SubtitleSearchHelper
import com.xyoye.common_component.utils.subtitle.SubtitleUtils
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.SubDetailData
import kotlinx.coroutines.launch

class ShooterSubtitleViewModel : BaseViewModel() {

    private val searchSubtitleRepository = SubtitleSearchHelper(viewModelScope)
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
        viewModelScope.launch {
            showLoading()
            val result = SourceRepository.getSubtitleDetail(
                SubtitleConfig.getShooterSecret().orEmpty(),
                subtitleId.toString()
            )
            hideLoading()

            if (result is Response.Error) {
                ToastCenter.showError(result.error.toastMsg)
                return@launch
            }

            val subtitle = result.dataOrNull?.sub?.subs?.firstOrNull()
            if (subtitle == null) {
                ToastCenter.showError("获取字幕详情失败")
                return@launch
            }

            searchSubDetailLiveData.postValue(subtitle)
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
                val unzipDirPath = SubtitleUtils.saveAndUnzipFile(fileName, responseBody.byteStream())
                if (unzipDirPath.isNullOrEmpty()) {
                    ToastCenter.showError("解压字幕文件失败，请尝试手动解压")
                } else {
                    ToastCenter.showSuccess("字幕下载成功：$unzipDirPath", Toast.LENGTH_LONG)
                }
            }

            onError { showNetworkError(it) }

            onComplete { hideLoading() }
        }
    }
}