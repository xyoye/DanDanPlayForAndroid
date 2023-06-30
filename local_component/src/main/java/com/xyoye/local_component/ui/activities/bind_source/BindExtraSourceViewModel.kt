package com.xyoye.local_component.ui.activities.bind_source

import androidx.collection.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.network.Retrofit
import com.xyoye.common_component.network.request.httpRequest
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.weight.ToastCenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException


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
        httpRequest<List<String>>(viewModelScope) {
            api {
                // 从缓存中获取
                val cache = segmentCache.get(storageFile.uniqueKey())
                if (cache != null && cache.isNotEmpty()) {
                    return@api cache
                }

                // 从网络获取
                val requestParams = JSONObject()
                requestParams.put("tasks", JSONArray().apply { put("tok") })
                requestParams.put("text", storageFile.fileName())
                val requestBody = requestParams.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val response = Retrofit.extService.segmentWords(params = requestBody)
                if (response.code() == 429) {
                    throw HttpException(response)
                }
                val json = response.body()?.string() ?: ""
                return@api parseSegmentResult(json) ?: emptyList()
            }

            onStart { showLoading() }

            onSuccess {
                // 缓存分词结果
                segmentCache.put(storageFile.uniqueKey(), it)
                _segmentTitleLiveData.postValue(it)
            }

            onError {
                if (it.code == 429) {
                    ToastCenter.showError("请求过于频繁(每分钟限2次)，请稍后再试")
                    return@onError
                }
                ToastCenter.showError("x${it.code} ${it.msg}")
            }

            onComplete { hideLoading() }
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