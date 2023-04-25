package com.xyoye.storage_component.ui.dialog

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.storage_component.ui.activities.storage_plus.StoragePlusActivity

/**
 * Created by xyoye on 2023/4/24
 */

abstract class StorageEditDialog<T : ViewDataBinding>(
    private val activity: StoragePlusActivity
) : BaseBottomDialog<T>(activity) {

    abstract fun onTestResult(result: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setOnDismissListener {
            doBeforeDismiss()
        }
    }

    open fun doBeforeDismiss() {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        activity.finish()
    }
}