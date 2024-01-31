package com.xyoye.anime_component.ui.fragment.search_magnet

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.MagnetRepository
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.MagnetData
import com.xyoye.data_component.entity.MagnetScreenEntity
import com.xyoye.data_component.entity.MagnetSearchHistoryEntity
import com.xyoye.data_component.enums.MagnetScreenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchMagnetFragmentViewModel : BaseViewModel() {

    val magnetTypeId = ObservableField<Int>()
    val magnetTypeText = ObservableField("全部分类")
    val magnetSubgroupId = ObservableField<Int>()
    val magnetSubgroupText = ObservableField("全部字幕组")

    val searchText = ObservableField<String>()

    val magnetLiveData = MutableLiveData<List<MagnetData>>()

    val searchHistoryLiveData = DatabaseManager.instance.getMagnetSearchHistoryDao().getAll()

    var magnetSubgroupData = MutableLiveData<List<MagnetScreenEntity>>()
    var magnetTypeData = MutableLiveData<List<MagnetScreenEntity>>()

    val domainErrorLiveData = MutableLiveData<Boolean>()

    fun search() {
        val keyword = searchText.get()
        if (keyword.isNullOrEmpty()) {
            ToastCenter.showWarning("请输入搜索条件")
            return
        }

        val magnetDomain = AppConfig.getMagnetResDomain()
        if (magnetDomain.isNullOrEmpty()) {
            domainErrorLiveData.postValue(true)
            return
        }

        viewModelScope.launch {
            DatabaseManager.instance.getMagnetSearchHistoryDao()
                .insert(MagnetSearchHistoryEntity(keyword))

            val typeId = magnetTypeId.get()?.toString().orEmpty()
            val subgroupId = magnetSubgroupId.get()?.toString().orEmpty()

            showLoading()
            val result = MagnetRepository.searchMagnet(magnetDomain, keyword, typeId, subgroupId)
            hideLoading()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            val resources = result.getOrNull()?.Resources ?: emptyList()
            magnetLiveData.postValue(resources)
        }
    }

    fun deleteSearchHistory(searchText: String) {
        viewModelScope.launch(context = Dispatchers.Main) {
            DatabaseManager.instance
                .getMagnetSearchHistoryDao()
                .deleteByText(searchText)
        }
    }

    fun deleteAllSearchHistory() {
        viewModelScope.launch(context = Dispatchers.Main) {
            DatabaseManager.instance
                .getMagnetSearchHistoryDao()
                .deleteAll()
        }
    }

    fun getMagnetSubgroup() {
        val subgroupData = magnetSubgroupData.value
        if (subgroupData != null) {
            magnetSubgroupData.postValue(subgroupData)
            return
        }

        val magnetDomain = AppConfig.getMagnetResDomain()
        if (magnetDomain.isNullOrEmpty()) {
            domainErrorLiveData.postValue(true)
            return
        }

        viewModelScope.launch {
            showLoading()
            val result = MagnetRepository.getMagnetSubgroup(magnetDomain)
            hideLoading()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            val subgroups = result.getOrNull()?.Subgroups?.map {
                MagnetScreenEntity(it.Id, it.Name, MagnetScreenType.SUBGROUP)
            } ?: emptyList()

            magnetSubgroupData.postValue(subgroups)
        }
    }

    fun getMagnetType() {
        val typeData = magnetTypeData.value
        if (typeData != null) {
            magnetTypeData.postValue(typeData)
            return
        }

        val magnetDomain = AppConfig.getMagnetResDomain()
        if (magnetDomain.isNullOrEmpty()) {
            domainErrorLiveData.postValue(true)
            return
        }

        viewModelScope.launch {
            showLoading()
            val result = MagnetRepository.getMagnetType(magnetDomain)
            hideLoading()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            val types = result.getOrNull()?.Types?.map {
                MagnetScreenEntity(it.Id, it.Name, MagnetScreenType.TYPE)
            } ?: emptyList()
            magnetTypeData.postValue(types)

        }
    }
}