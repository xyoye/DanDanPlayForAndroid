package com.xyoye.local_component.ui.activities.bind_source

import androidx.collection.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.collectable
import com.xyoye.common_component.extension.toastError
import com.xyoye.common_component.network.repository.OtherRepository
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.weight.ToastCenter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject


/**
 * Created by xyoye on 2022/1/24
 */
class BindExtraSourceViewModel : BaseViewModel() {

    companion object {
        private const val MAX_CACHE_SIZE = 50

        // 分词结果缓存
        private val segmentCache = LruCache<String, List<String>>(MAX_CACHE_SIZE)
    }

    private lateinit var storageFile: StorageFile

    val storageFileFlow: StateFlow<StorageFile> by lazy {
        DatabaseManager.instance.getPlayHistoryDao().getPlayHistoryFlow(
            storageFile.uniqueKey(),
            storageFile.storage.library.id
        ).map {
            storageFile.clone().apply { playHistory = it }
        }.stateIn(viewModelScope, SharingStarted.Lazily, storageFile)
    }

    private val _searchTextFlow = MutableSharedFlow<String>()
    val searchTextFlow = _searchTextFlow.collectable

    private val _segmentTitleLiveData = MediatorLiveData<List<String>>()
    val segmentTitleLiveData: LiveData<List<String>> = _segmentTitleLiveData

    fun setStorageFile(storageFile: StorageFile) {
        this.storageFile = storageFile
    }

    fun setSearchText(text: String) {
        viewModelScope.launch {
            _searchTextFlow.emit(text)
        }
    }

    fun segmentTitle(storageFile: StorageFile) {
        viewModelScope.launch {
            // 从缓存中获取
            val cache = segmentCache.get(storageFile.uniqueKey()) ?: emptyList()
            if (cache.isNotEmpty()) {
                _segmentTitleLiveData.postValue(cache)
                return@launch
            }

            // 从网络获取
            showLoading()
            val result = OtherRepository.getSegmentWords(storageFile.fileName())
            hideLoading()

            if (result.isFailure) {
                result.exceptionOrNull()?.message?.toastError()
                return@launch
            }

            if (result.isSuccess) {
                if (result.getOrThrow().code() == 409) {
                    ToastCenter.showError("请求过于频繁(每分钟限2次)，请稍后再试")
                    return@launch
                }

                val json = result.getOrThrow().body()?.string() ?: ""
                val segments = parseSegmentResult(json) ?: emptyList()
                segmentCache.put(storageFile.uniqueKey(), segments)
                _segmentTitleLiveData.postValue(segments)
            }
        }
    }

    /**
     * 解析分词结果
     */
    private fun parseSegmentResult(json: String): List<String>? {
        val responseJson = JSONObject(json)
        val resultKey = responseJson.names()?.get(0)?.toString()
            ?: return null
        val jsonArray = responseJson.optJSONArray(resultKey)
            ?: return null
        val wordArray = jsonArray.optJSONArray(0)
            ?: return null

        val words = mutableListOf<String>()
        val wordLength = wordArray.length()
        for (i in 0 until wordLength) {
            val word = wordArray.optString(i)
            words.add(word)
        }
        return words
    }
}