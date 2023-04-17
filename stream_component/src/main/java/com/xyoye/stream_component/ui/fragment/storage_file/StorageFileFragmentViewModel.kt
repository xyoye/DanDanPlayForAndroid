package com.xyoye.stream_component.ui.fragment.storage_file

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.storage.Storage
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.utils.storage.StorageSortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StorageFileFragmentViewModel : BaseViewModel() {
    companion object {
        private val lastPlayDirectory = PlayHistoryEntity(
            url = "",
            mediaType = MediaType.OTHER_STORAGE,
            videoName = "",
            isLastPlay = true
        )
    }

    private val _fileLiveData = MutableLiveData<List<StorageFile>>()
    val fileLiveData: LiveData<List<StorageFile>> = _fileLiveData

    lateinit var storage: Storage

    //当前媒体库中最后一次播放记录
    private var storageLastPlay: PlayHistoryEntity? = null

    // 是否隐藏.开头的文件
    private val hidePointFile = AppConfig.isShowHiddenFile().not()

    // 文件排序选项
    private var sortOption = StorageSortOption()

    // 文件列表快照
    private var filesSnapshot = listOf<StorageFile>()

    /**
     * 展开文件夹
     */
    fun listFile(directory: StorageFile?, refresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val target = directory ?: storage.getRootFile()
            if (target == null) {
                emptyList<StorageFile>()
                    .apply { _fileLiveData.postValue(this) }
                    .also { filesSnapshot = it }
                return@launch
            }

            refreshStorageLastPlay()
            storage.openDirectory(target, refresh)
                .filter { isDisplayFile(it) }
                .sortedWith(sortOption.createComparator())
                .onEach { it.playHistory = getHistory(it) }
                .apply { _fileLiveData.postValue(this) }
                .also { filesSnapshot = it }
        }
    }

    /**
     * 修改文件排序
     */
    fun changeSortOption(option: StorageSortOption) {
        sortOption = option
        viewModelScope.launch(Dispatchers.IO) {
            val currentFiles = _fileLiveData.value ?: return@launch
            mutableListOf<StorageFile>()
                .plus(currentFiles)
                .sortedWith(sortOption.createComparator())
                .apply { _fileLiveData.postValue(this) }
                .also { filesSnapshot = it }
        }
    }

    /**
     * 搜索文件
     */
    fun searchByText(text: String) {
        //媒体库支持文件搜索，由媒体库处理搜索
        if (storage.supportSearch()) {
            viewModelScope.launch(Dispatchers.IO) {
                refreshStorageLastPlay()
                storage.search(text)
                    .filter { isDisplayFile(it) }
                    .sortedWith(sortOption.createComparator())
                    .onEach { it.playHistory = getHistory(it) }
                    .let { _fileLiveData.postValue(it) }
            }
            return
        }

        //搜索条件为空，返回文件列表快照
        if (text.isEmpty()) {
            _fileLiveData.postValue(filesSnapshot)
            return
        }

        //在当前文件列表进行搜索
        val currentFiles = _fileLiveData.value ?: return
        mutableListOf<StorageFile>()
            .plus(currentFiles)
            .filter { it.fileName().contains(text) }
            .let { _fileLiveData.postValue(it) }
    }

    /**
     * 解绑资源文件
     */
    fun unbindExtraSource(file: StorageFile, unbindDanmu: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (unbindDanmu) {
                DatabaseManager.instance.getPlayHistoryDao().updateDanmu(
                    file.uniqueKey(), storage.library.mediaType, null, 0
                )
            } else {
                DatabaseManager.instance.getPlayHistoryDao().updateSubtitle(
                    file.uniqueKey(), storage.library.mediaType, null
                )
            }
            updateHistory()
        }
    }

    /**
     * 更新文件相关的播放历史
     */
    fun updateHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val fileList = _fileLiveData.value ?: return@launch

            refreshStorageLastPlay()
            fileList
                .map {
                    val history = getHistory(it)
                    val isSameHistory = if (it.isFile()) {
                        it.playHistory == history
                    } else {
                        it.playHistory?.id == history?.id
                    }
                    if (isSameHistory) {
                        return@map it
                    }
                    //历史记录不一致时，返回拷贝的新对象
                    it.clone().apply { playHistory = history }
                }.apply { _fileLiveData.postValue(this) }
                .also { filesSnapshot = it }
        }
    }

    /**
     * 获取文件播放历史
     */
    private suspend fun getHistory(file: StorageFile): PlayHistoryEntity? {
        if (file.isDirectory()) {
            return lastPlayDirectoryHistory(file)
        }
        if (file.isVideoFile().not()) {
            return null
        }
        var history = DatabaseManager.instance
            .getPlayHistoryDao()
            .getPlayHistory(file.uniqueKey(), file.storage.library.id)
        if (history == null) {
            //这是一步补救措施，数据库11版本之前，没有存储storageId字段
            //因此为了避免弹幕等历史数据无法展示，依旧需要通过mediaType查询
            history = DatabaseManager.instance
                .getPlayHistoryDao()
                .getPlayHistory(file.uniqueKey(), file.storage.library.mediaType)
            //补充storageId字段
            if (history != null) {
                history.storageId = file.storage.library.id
                DatabaseManager.instance.getPlayHistoryDao().insert(history)
            }
        }
        if (history != null && storageLastPlay != null) {
            history.isLastPlay = history.id == storageLastPlay!!.id
        }
        return history
    }

    /**
     * 刷新最后一次播放记录
     */
    private suspend fun refreshStorageLastPlay() {
        storageLastPlay = DatabaseManager.instance
            .getPlayHistoryDao()
            .gitStorageLastPlay(storage.library.id)
        storageLastPlay?.isLastPlay = true
    }

    /**
     * 文件夹是否为最后一次播放记录的父文件夹
     * 是：返回最后播放的标签
     * 否：null
     */
    private fun lastPlayDirectoryHistory(file: StorageFile): PlayHistoryEntity? {
        val lastPlayStoragePath = storageLastPlay?.storagePath
            ?: return null
        if (TextUtils.isEmpty(lastPlayStoragePath)) {
            return null
        }
        if (file.isStoragePathParent(lastPlayStoragePath).not()) {
            return null
        }
        return lastPlayDirectory
    }

    /**
     * 是否可展示的文件
     */
    private fun isDisplayFile(storageFile: StorageFile): Boolean {
        //.开头的文件，根据配置展示
        if (hidePointFile && storageFile.fileName().startsWith(".")) {
            return false
        }
        //文件夹，展示
        if (storageFile.isDirectory()) {
            return true
        }
        //视频文件，展示
        return storageFile.isVideoFile()
    }
}