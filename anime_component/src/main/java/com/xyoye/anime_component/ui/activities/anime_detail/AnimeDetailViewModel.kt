package com.xyoye.anime_component.ui.activities.anime_detail

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.UserConfig
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.AnimeRepository
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.BangumiData
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class AnimeDetailViewModel : BaseViewModel() {

    val animeIdField = ObservableField<String>()
    val animeTitleField = ObservableField<String>()
    val animeStatusField = ObservableField<String>()
    val animeRateField = ObservableField<String>()
    val animeTypeField = ObservableField<String>()

    val followLiveData = MutableLiveData<Boolean>()

    val animeDetailLiveData = MutableLiveData<BangumiData>()

    fun getAnimeDetail(animeId: String) {
        viewModelScope.launch {
            val result = AnimeRepository.getAnimeDetail(animeId)

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            result.getOrNull()?.bangumi?.apply {
                animeTitleField.set(animeTitle)
                animeStatusField.set(if (isOnAir) "状态：连载中" else "状态：已完结")
                animeTypeField.set("类型：$typeDescription")
                animeRateField.set("评分：${getRating(rating)}")

                followLiveData.postValue(isFavorited)
                animeDetailLiveData.postValue(this)
            }
        }
    }

    fun followAnime() {
        if (UserConfig.isUserLoggedIn().not()) {
            ToastCenter.showWarning("请先登录后再进行此操作")
            return
        }

        viewModelScope.launch {
            val isFollowed = followLiveData.value ?: false
            val animeId = animeIdField.get()!!

            val result = if (isFollowed) {
                AnimeRepository.cancelFollowAnime(animeId)
            } else {
                AnimeRepository.followAnime(animeId)
            }

            if (result.isFailure) {
                val status = if (isFollowed) "取消关注" else "关注"
                ToastCenter.showError("${status}失败，请重试")
                return@launch
            }

            followLiveData.postValue(!isFollowed)
        }
    }

    private fun getRating(rating: Double) =
        if (rating <= 0)
            "暂无"
        else
            DecimalFormat("0.0").format(rating)
}