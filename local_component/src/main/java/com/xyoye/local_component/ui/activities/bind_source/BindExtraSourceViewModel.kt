package com.xyoye.local_component.ui.activities.bind_source

import androidx.collection.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.network.repository.OtherRepository
import com.xyoye.common_component.network.request.Response
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.weight.ToastCenter
import kotlinx.coroutines.Dispatchers
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

    private val _historyChangedLiveData = MediatorLiveData<StorageFile>()
    val historyChangedLiveData: LiveData<StorageFile> = _historyChangedLiveData

    private val _segmentTitleLiveData = MediatorLiveData<List<String>>()
    val segmentTitleLiveData: LiveData<List<String>> = _segmentTitleLiveData

    fun updateSourceChanged(storageFile: StorageFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val history = DatabaseManager.instance.getPlayHistoryDao().getPlayHistory(
                storageFile.uniqueKey(),
                storageFile.storage.library.id
            )
            val newStorageFile = storageFile.clone().apply {
                playHistory = history
            }
            _historyChangedLiveData.postValue(newStorageFile)
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

            if (result is Response.Error) {
                ToastCenter.showError(result.error.toastMsg)
                return@launch
            }

            if (result is Response.Success) {
                if (result.data.code() == 409) {
                    ToastCenter.showError("请求过于频繁(每分钟限2次)，请稍后再试")
                    return@launch
                }

                val json = result.data.body()?.string() ?: ""
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