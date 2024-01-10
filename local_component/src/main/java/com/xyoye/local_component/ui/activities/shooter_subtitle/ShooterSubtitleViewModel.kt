package com.xyoye.local_component.ui.activities.shooter_subtitle

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.network.repository.ResourceRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.network.request.dataOrNull
import com.xyoye.common_component.utils.subtitle.SubtitleSearchHelper
import com.xyoye.common_component.utils.subtitle.SubtitleUtils
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.SubDetailData
import kotlinx.coroutines.Dispatchers
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
            val result = ResourceRepository.getSubtitleDetail(
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
        viewModelScope.launch(Dispatchers.IO) {
            showLoading()
            val result = ResourceRepository.getResourceResponseBody(downloadUrl)
            val subtitlePath = result.dataOrNull?.byteStream()?.let {
                SubtitleUtils.saveSubtitle(fileName, it)
            }
            hideLoading()

            if (subtitlePath.isNullOrEmpty()) {
                ToastCenter.showError("保存字幕失败")
                return@launch
            }

            ToastCenter.showSuccess("字幕下载成功：$subtitlePath", Toast.LENGTH_LONG)
        }
    }

    /**
     * 下载压缩文件，并解压
     */
    fun downloadAndUnzipFile(fileName: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading()
            val result = ResourceRepository.getResourceResponseBody(url)
            val unzipDirPath = result.dataOrNull?.byteStream()?.let {
                SubtitleUtils.saveAndUnzipFile(fileName, it)
            }
            hideLoading()

            if (unzipDirPath.isNullOrEmpty()) {
                ToastCenter.showError("解压字幕文件失败，请尝试手动解压")
                return@launch
            }

            ToastCenter.showSuccess("字幕下载成功：$unzipDirPath", Toast.LENGTH_LONG)
        }
    }
}