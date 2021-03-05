package com.xyoye.local_component.ui.activities.shooter_subtitle

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.RequestError
import com.xyoye.common_component.network.request.RequestErrorHandler
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.utils.SubtitleUtils
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.SubDetailData
import com.xyoye.data_component.data.SubtitleSearchData
import com.xyoye.data_component.data.SubtitleSubData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import retrofit2.HttpException

@FlowPreview
@ExperimentalCoroutinesApi
class ShooterSubtitleViewModel : BaseViewModel() {
    val searchSubDetailLiveData = MutableLiveData<SubDetailData>()
    val searchSubtitleChannel = ConflatedBroadcastChannel<String>()

    val searchSubtitleLiveData = searchSubtitleChannel.asFlow()
        //避免快速请求
        .debounce(200)
        .flatMapLatest { searchSubtitle(it) }
        .asLiveData()

    /**
     * 搜索字幕
     */
    private fun searchSubtitle(videoName: String): Flow<PagingData<SubtitleSearchData>> {
        return Pager(PagingConfig(15, 15), 1) {
            object : PagingSource<Int, SubtitleSearchData>() {
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SubtitleSearchData> {
                    val page = params.key ?: return LoadResult.Page(mutableListOf(), null, null)
                    return try {
                        val shooterSecret = SubtitleConfig.getShooterSecret() ?: ""
                        val subData =
                            Retrofit.extService.searchSubtitle(shooterSecret, videoName, page)
                        hideLoading()
                        LoadResult.Page(sub2SubtitleSearchData(subData), null, page + 1)
                    } catch (e: Exception) {
                        hideLoading()
                        //处理509异常
                        if (e is HttpException && e.code() == 509) {
                            val limitError = RequestError(509, "请求频率过高")
                            LoadResult.Error(limitError)
                        } else {
                            LoadResult.Error(RequestErrorHandler(e).handlerError())
                        }
                    }
                }
            }
        }.flow.cachedIn(viewModelScope)
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

    /**
     * 搜索字幕转显示数据类型
     */
    private fun sub2SubtitleSearchData(subData: SubtitleSubData?): MutableList<SubtitleSearchData> {
        return mutableListOf<SubtitleSearchData>().apply {
            val subList = subData?.sub?.subs
            if (subList?.size ?: 0 > 0) {
                for (subDetailData in subList!!) {
                    val subtitleName =
                        if (subDetailData.native_name.isNullOrEmpty()) subDetailData.videoname else subDetailData.native_name
                    add(
                        SubtitleSearchData(
                            subDetailData.id,
                            subtitleName,
                            subDetailData.upload_time,
                            subDetailData.subtype,
                            subDetailData.lang?.desc
                        )
                    )
                }
            }
        }
    }
}