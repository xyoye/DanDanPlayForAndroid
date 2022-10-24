package com.xyoye.common_component.utils.aliyun

import android.app.Application
import com.taobao.update.apk.ApkUpdater
import com.xyoye.common_component.weight.dialog.AppUpdateDialog

/**
 * Created by xyoye on 2022/10/25.
 */

object UpdateDialogManager {
    private var updateDialog: AppUpdateDialog? = null

    fun init(application: Application) {
        ApkUpdater(application).apply {
            //监听下载进度
            setApkDownloadListener(UpdateProgressObserver {
                updateDialog?.updateProgress(it)
            })

            //调起更新弹窗
            setUpdateNotifyListener { activity, updateInfo, userAction ->
                if (activity == null) {
                    return@setUpdateNotifyListener
                }

                if (updateDialog != null && updateDialog!!.isShowing) {
                    updateDialog!!.dismiss()
                }

                val status = if (updateInfo.isForceUpdate)
                    AppUpdateDialog.Status.UpdateForce
                else
                    AppUpdateDialog.Status.Update
                updateDialog = AppUpdateDialog(activity, updateInfo, status).apply {
                    setPositive { userAction.onConfirm() }
                    setNegative { userAction.onCancel() }
                }
                updateDialog!!.show()
            }

            //取消更新更新弹窗
            setCancelUpdateNotifyListener(null)

            //安装弹窗
            setInstallUpdateNotifyListener { activity, updateInfo, userAction ->
                if (activity == null) {
                    return@setInstallUpdateNotifyListener
                }

                if (updateDialog != null && updateDialog!!.isShowing) {
                    updateDialog!!.dismiss()
                }

                val status = AppUpdateDialog.Status.Install
                updateDialog = AppUpdateDialog(activity, updateInfo, status).apply {
                    setPositive { userAction.onConfirm() }
                    setNegative { userAction.onCancel() }
                }
                updateDialog!!.show()
            }
        }
    }
}