package com.xyoye.stream_component.ui.dialog

import androidx.databinding.ViewDataBinding
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.stream_component.ui.activities.storage_plus.StoragePlusActivity

/**
 * Created by xyoye on 2023/4/24
 */

abstract class StorageEditDialog<T : ViewDataBinding>(
    activity: StoragePlusActivity
) : BaseBottomDialog<T>(activity) {

    abstract fun onTestResult(result: Boolean)
}