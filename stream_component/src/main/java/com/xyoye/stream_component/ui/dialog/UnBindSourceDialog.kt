package com.xyoye.stream_component.ui.dialog

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.enums.MediaType
import com.xyoye.data_component.enums.SheetActionType
import com.xyoye.stream_component.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Created by xyoye on 2022/1/18
 */
object UnBindSourceDialogUtils {
    private const val ACTION_UNBIND_DANMU = 1
    private const val ACTION_UNBIND_SUBTITLE = 2

    fun show(
        activity: AppCompatActivity,
        mediaType: MediaType,
        uniqueKey: String?,
        danmuPath: String?,
        subtitlePath: String?,
        afterUnbindSource: () -> Unit,
    ): Boolean {
        if (uniqueKey.isNullOrEmpty()) {
            return false
        }
        val actionList = mutableListOf<SheetActionBean>()

        if (!danmuPath.isNullOrEmpty()) {
            actionList.add(
                SheetActionBean(
                    ACTION_UNBIND_DANMU,
                    "移除弹幕绑定",
                    R.drawable.ic_unbind_danmu
                )
            )
        }
        if (!subtitlePath.isNullOrEmpty()) {
            actionList.add(
                SheetActionBean(
                    ACTION_UNBIND_SUBTITLE,
                    "移除字幕绑定",
                    R.drawable.ic_unbind_subtitle
                )
            )
        }
        if (actionList.isEmpty())
            return false

        BottomActionDialog(actionList, SheetActionType.VERTICAL) {
            when (it) {
                ACTION_UNBIND_DANMU -> unbindDanmu(
                    activity,
                    mediaType,
                    uniqueKey,
                    afterUnbindSource
                )
                ACTION_UNBIND_SUBTITLE -> unbindSubtitle(
                    activity,
                    mediaType,
                    uniqueKey,
                    afterUnbindSource
                )
            }
            return@BottomActionDialog true
        }.show(activity)

        return true
    }

    private fun unbindDanmu(
        activity: AppCompatActivity,
        mediaType: MediaType,
        uniqueKey: String,
        afterUnbindSource: () -> Unit
    ) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            DatabaseManager.instance.getPlayHistoryDao().updateDanmuByKey(
                uniqueKey, mediaType, null, 0
            )
            afterUnbindSource.invoke()
        }
    }

    private fun unbindSubtitle(
        activity: AppCompatActivity,
        mediaType: MediaType,
        uniqueKey: String,
        afterUnbindSource: () -> Unit
    ) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            DatabaseManager.instance.getPlayHistoryDao().updateSubtitleByKey(
                uniqueKey, mediaType, null
            )
            afterUnbindSource.invoke()
        }
    }
}