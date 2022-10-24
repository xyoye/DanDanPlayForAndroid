package com.xyoye.common_component.weight.dialog

import android.app.Dialog
import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.taobao.update.common.dialog.CustomUpdateInfo
import com.xyoye.common_component.R
import com.xyoye.common_component.databinding.DialogAppUpdateBinding

/**
 * Created by xyoye on 2022/10/24.
 */


class AppUpdateDialog(
    context: Context,
    private val updateInfo: CustomUpdateInfo,
    private val status: Status = Status.Update
) : Dialog(context, R.style.UpdateDialog) {

    enum class Status {
        Update,

        UpdateForce,

        Updating,

        Install
    }

    private val dataBinding: DialogAppUpdateBinding = DataBindingUtil.inflate(
        layoutInflater,
        R.layout.dialog_app_update,
        null,
        false
    )

    private var positiveBlock: (() -> Unit)? = null
    private var negativeBlock: (() -> Unit)? = null

    init {
        setContentView(dataBinding.root)

        setCancelable(false)
        setCanceledOnTouchOutside(false)

        initWindow()

        initListener()

        setupStatus(status)

        setupUpdateInfo()
    }

    private fun initWindow() {
        window?.apply {
            decorView.setPadding(0, decorView.top, 0, decorView.bottom)

            val layoutParams = attributes
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            attributes = layoutParams

            setGravity(Gravity.CENTER)
        }
    }

    private fun initListener() {
        dataBinding.tvUpdate.setOnClickListener {
            positiveBlock?.invoke()
            setupStatus(Status.Updating)
            dataBinding.tvProgress.text = "下载中：0%"
        }

        dataBinding.tvInstall.setOnClickListener {
            positiveBlock?.invoke()
            dismiss()
        }

        dataBinding.ivDialogClose.setOnClickListener {
            negativeBlock?.invoke()
            dismiss()
        }
    }

    private fun setupStatus(status: Status) {
        dataBinding.tvUpdate.isVisible = status == Status.Update || status == Status.UpdateForce
        dataBinding.tvInstall.isVisible = status == Status.Install
        dataBinding.tvProgress.isVisible = status == Status.Updating
        dataBinding.viewProgress.isVisible = status == Status.Updating
        dataBinding.ivDialogClose.isVisible = updateInfo.isForceUpdate.not()
    }

    private fun setupUpdateInfo() {
        val versionDisplay = "v${updateInfo.version}"
        dataBinding.tvVersion.text = versionDisplay
        dataBinding.tvUpdateContent.text = updateInfo.info

        dataBinding.tvUpdateContent.movementMethod = ScrollingMovementMethod()
    }

    fun setPositive(block: () -> Unit) {
        this.positiveBlock = block
    }

    fun setNegative(block: () -> Unit) {
        this.negativeBlock = block
    }

    fun updateProgress(progress: Int) {
        if (status == Status.Install || isShowing.not()) {
            return
        }

        dataBinding.tvProgress.post {
            setupStatus(Status.Updating)
            val tips = "下载中：$progress%"
            dataBinding.tvProgress.text = tips
            dataBinding.viewProgress.progress = progress
        }
    }

}