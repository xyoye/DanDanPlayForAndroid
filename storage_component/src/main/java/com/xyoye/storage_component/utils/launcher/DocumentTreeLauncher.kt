package com.xyoye.storage_component.utils.launcher

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Created by xyoye on 2022/12/31.
 */

class DocumentTreeLauncher(
    private val activity: FragmentActivity,
    private val onResult: (Uri?) -> Unit
) : DefaultLifecycleObserver {
    companion object {
        private const val KEY_OPEN_DOCUMENT_TREE = "key_open_document_tree"
    }

    private lateinit var openDocumentTree: ActivityResultLauncher<Uri?>

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(lifecycleOwner: LifecycleOwner) {
        openDocumentTree = activity.activityResultRegistry.register(
            KEY_OPEN_DOCUMENT_TREE,
            lifecycleOwner,
            ActivityResultContracts.OpenDocumentTree()
        ) {
            onResult.invoke(it)
        }
    }

    fun launch() {
        openDocumentTree.launch(null)
    }
}