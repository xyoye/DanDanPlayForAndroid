package com.xyoye.stream_component.ui.activities.document_tree

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.launch

class DocumentTreeViewModel : BaseViewModel() {
    private val _exitLiveData = MutableLiveData<Any>()
    var exitLiveData: LiveData<Any> = _exitLiveData

    fun addExternalStorage(documentFile: DocumentFile) {
        val externalLibraryEntity = MediaLibraryEntity(
            displayName = getDisplayName(documentFile),
            url = documentFile.uri.toString(),
            describe = getDescribe(documentFile),
            mediaType = MediaType.EXTERNAL_STORAGE
        )

        viewModelScope.launch {
            DatabaseManager.instance
                .getMediaLibraryDao()
                .insert(externalLibraryEntity)
            _exitLiveData.postValue(Any())
        }
    }

    private fun getDisplayName(documentFile: DocumentFile): String {
        val treeId = getTreeDocumentId(documentFile.uri)
        if (treeId == "primary:") {
            return "内部存储设备"
        }
        return documentFile.name ?: documentFile.uri.path ?: documentFile.uri.toString()
    }

    private fun getDescribe(documentFile: DocumentFile): String {
        return documentFile.uri.path ?: documentFile.uri.toString()
    }

    private fun getTreeDocumentId(documentUri: Uri): String? {
        val paths = documentUri.pathSegments
        if (paths.size >= 2 && "tree" == paths[0]) {
            return paths[1]
        }
        return null
    }
}