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
import java.util.LinkedList


/**
 * Created by xyoye on 2022/1/24
 */
class BindExtraSourceViewModel : BaseViewModel() {

    companion object {
        private const val MAX_SEGMENT_CACHE_SIZE = 50
        private const val MAX_SEARCH_TEXT_CACHE_SIZE = 25

        // 分词结果缓存
        private val segmentCache = LruCache<String, List<String>>(MAX_SEGMENT_CACHE_SIZE)

        // 搜索记录缓存，格式为 <file_directory, file_name, searched_text>。
        private val searchTextCache = LinkedList<Triple<String, String, String>>()
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

    private val _searchTextFlow = MutableSharedFlow<String>(1)
    val searchTextFlow = _searchTextFlow.collectable

    private val _segmentTitleLiveData = MediatorLiveData<List<String>>()
    val segmentTitleLiveData: LiveData<List<String>> = _segmentTitleLiveData

    fun setStorageFile(storageFile: StorageFile) {
        this.storageFile = storageFile
        val cachedSearchText: String? = matchSearchTextCache(storageFile)
        if (cachedSearchText != null) {
            viewModelScope.launch {
                _searchTextFlow.emit(cachedSearchText)
            }
        }
    }

    fun setSearchText(text: String) {
        viewModelScope.launch {
            _searchTextFlow.emit(text)
        }
        addSearchTextCache(storageFile, text)
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

            val data = result.getOrNull() ?: return@launch
            if (data.code() == 409) {
                ToastCenter.showError("请求过于频繁(每分钟限2次)，请稍后再试")
                return@launch
            }
            val json = data.body()?.string() ?: ""
            val segments = parseSegmentResult(json) ?: emptyList()
            segmentCache.put(storageFile.uniqueKey(), segments)
            _segmentTitleLiveData.postValue(segments)
        }
    }

    /**
     * 解析分词结果
     */
    private fun parseSegmentResult(json: String): List<String>? {
        if (json.isEmpty()) {
            return null
        }

        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun matchSearchTextCache(target: StorageFile): String? {
        for (cachedTriple in searchTextCache) {
            val cachedFileDir: String = cachedTriple.first
            val cachedFileName: String = cachedTriple.second
            val cachedText: String = cachedTriple.third
            val targetDir = parseFileDir(target.filePath())
            val targetName = target.fileName()
            if (!target.isFile()) {
                continue
            }
            // 比较所在目录。
            if (targetDir != cachedFileDir) {
                continue
            }
            // 比较文件名。
            var ptr = 0
            val diff = LinkedList<Int>()
            while (ptr < targetName.length || ptr < cachedFileName.length) {
                val a = if (ptr < targetName.length) targetName[ptr] else '*'
                val b = if (ptr < cachedFileName.length) cachedFileName[ptr] else '*'
                if (a != b) {
                    diff.addLast(ptr)
                }
                ptr++
            }
            if (diff.size == 0) {
                // 无差异。
                return cachedText
            }
            if (diff.size > 2) {
                // 差异过多。
                continue
            }
            if (diff.any { !targetName[it].isDigit() || !cachedFileName[it].isDigit() }) {
                // 差异包含数字以外的内容。
                continue
            }
            if (diff.size == 2 && (diff[1] - diff[0] != 1)) {
                // 多段不相邻差异。
                continue
            }
            // 命中缓存。
            if (cachedText.matches(Regex(".* \\d{1,2}$"))) {
                // 缓存搜索结果末尾包含集数，删除集数内容。
                // 不作替换处理是避免错误处理文件名中类似"11"、"12"这样的差异。
                return cachedText.replace(Regex(" \\d{1,2}$"), "")
            }
            return cachedText
        }
        return null
    }

    private fun addSearchTextCache(file: StorageFile, text: String) {
        val dir = parseFileDir(file.filePath())
        val name = file.fileName()
        searchTextCache.addFirst(Triple(dir, name, text))
        if (searchTextCache.size > MAX_SEARCH_TEXT_CACHE_SIZE) {
            searchTextCache.removeLast()
        }
    }

    private fun parseFileDir(fullPath: String): String {
        if (fullPath.contains(Regex("/"))) {
            return fullPath.replace(Regex("/[^/]*$"), "")
        }
        return "/"
    }
}