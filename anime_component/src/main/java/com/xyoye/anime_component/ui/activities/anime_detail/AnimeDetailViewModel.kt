package com.xyoye.anime_component.ui.activities.anime_detail

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.UserConfig
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.AnimeDetailData
import com.xyoye.data_component.data.BangumiData
import com.xyoye.data_component.data.CommonJsonData
import java.text.DecimalFormat

class AnimeDetailViewModel : BaseViewModel() {

    val animeIdField = ObservableField<String>()
    val animeTitleField = ObservableField<String>()
    val animeStatusField = ObservableField<String>()
    val animeRateField = ObservableField<String>()
    val animeTypeField = ObservableField<String>()

    val followLiveData = MutableLiveData<Boolean>()

    val animeDetailLiveData = MutableLiveData<BangumiData>()

    val transitionFailedLiveData = MutableLiveData<Boolean>()

    fun getAnimeDetail(animeId: String) {
        httpRequest<AnimeDetailData>(viewModelScope) {
            api {
                Retrofit.service.getAnimeDetail(animeId)
            }

            onSuccess {
                it.bangumi?.apply {
                    animeTitleField.set(animeTitle)
                    animeStatusField.set(if (isOnAir) "状态：连载中" else "状态：已完结")
                    animeTypeField.set("类型：$typeDescription")
                    animeRateField.set("评分：${getRating(rating)}")

                    followLiveData.postValue(isFavorited)
                    animeDetailLiveData.postValue(this)
                }
            }

            onError {
                showNetworkError(it)
                transitionFailedLiveData.postValue(true)
            }
        }
    }

    fun followAnime() {
        val isFollowed = followLiveData.value ?: false
        if (UserConfig.isUserLoggedIn()) {
            httpRequest<CommonJsonData>(viewModelScope) {
                api {
                    if (isFollowed) {
                        Retrofit.service.unFollow(animeIdField.toString())
                    } else {
                        val map = HashMap<String, String>()
                        map["animeId"] = animeIdField.toString()
                        map["favoriteStatus"] = "favorited"
                        map["rating"] = "0"
                        Retrofit.service.follow(map)
                    }
                }

                onSuccess {
                    followLiveData.postValue(!isFollowed)
                }

                onError {
                    val status = if (isFollowed) "取消关注" else "关注"
                    ToastCenter.showError("${status}失败，请重试")
                }
            }
        } else {
            ToastCenter.showWarning("请先登录后再进行此操作")
        }
    }

    private fun getRating(rating: Double) =
        if (rating <= 0)
            "暂无"
        else
            DecimalFormat("0.0").format(rating)
}