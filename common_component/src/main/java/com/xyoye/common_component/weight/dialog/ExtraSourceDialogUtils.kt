package com.xyoye.common_component.weight.dialog

import androidx.lifecycle.viewModelScope
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.R
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.bean.StorageFileBean
import com.xyoye.data_component.enums.MediaType
import com.xyoye.data_component.enums.SheetActionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Created by xyoye on 2022/1/18
 */
object ExtraSourceDialogUtils {
    private const val ACTION_BIND_DANMU = 1
    private const val ACTION_BIND_SUBTITLE = 2
    private const val ACTION_UNBIND_DANMU = 3
    private const val ACTION_UNBIND_SUBTITLE = 4

    fun show(
        activity: BaseActivity<*, *>,
        mediaType: MediaType,
        data: StorageFileBean,
        onSourceChanged: () -> Unit,
    ): Boolean {
        val uniqueKey = data.uniqueKey
        if (uniqueKey.isNullOrEmpty()) {
            return false
        }
        val actionList = mutableListOf(
            SheetActionBean(
                ACTION_BIND_DANMU,
                "手动查找弹幕",
                R.drawable.ic_bind_danmu_manual
            ),
            SheetActionBean(
                ACTION_BIND_SUBTITLE,
                "手动查找字幕",
                R.drawable.ic_bind_subtitle
            )
        )

        if (!data.danmuPath.isNullOrEmpty()) {
            actionList.add(
                SheetActionBean(
                    ACTION_UNBIND_DANMU,
                    "移除弹幕绑定",
                    R.drawable.ic_unbind_danmu
                )
            )
        }
        if (!data.subtitlePath.isNullOrEmpty()) {
            actionList.add(
                SheetActionBean(
                    ACTION_UNBIND_SUBTITLE,
                    "移除字幕绑定",
                    R.drawable.ic_unbind_subtitle
                )
            )
        }

        val viewModelScope = activity.getOwnerViewModel().viewModelScope
        BottomActionDialog(actionList, SheetActionType.VERTICAL) {
            when (it) {
                ACTION_BIND_DANMU -> bindDanmu(mediaType, data)
                ACTION_BIND_SUBTITLE -> bindSubtitle(mediaType, data)
                ACTION_UNBIND_DANMU -> unbindDanmu(viewModelScope, mediaType, uniqueKey, onSourceChanged)
                ACTION_UNBIND_SUBTITLE -> unbindSubtitle(viewModelScope, mediaType, uniqueKey, onSourceChanged)
            }
            return@BottomActionDialog true
        }.show(activity)

        return true
    }

    private fun bindDanmu(
        mediaType: MediaType,
        data: StorageFileBean
    ) {
        val videoPath = if (mediaType == MediaType.LOCAL_STORAGE) {
            data.filePath
        } else {
            ""
        }
        ARouter.getInstance()
            .build(RouteTable.Local.BindDanmu)
            .withString("videoName", data.fileName)
            .withString("videoPath", videoPath)
            .navigation()
    }

    private fun bindSubtitle(
        mediaType: MediaType,
        data: StorageFileBean
    ) {
        val videoPath = if (mediaType == MediaType.LOCAL_STORAGE) {
            data.filePath
        } else {
            ""
        }
        ARouter.getInstance()
            .build(RouteTable.Local.BindSubtitle)
            .withString("videoPath", videoPath)
            .navigation()
    }

    private fun unbindDanmu(
        scope: CoroutineScope,
        mediaType: MediaType,
        uniqueKey: String,
        onSourceChanged: () -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            DatabaseManager.instance.getPlayHistoryDao().updateDanmuByKey(
                uniqueKey, mediaType, null, 0
            )
            onSourceChanged.invoke()
        }
    }

    private fun unbindSubtitle(
        scope: CoroutineScope,
        mediaType: MediaType,
        uniqueKey: String,
        onSourceChanged: () -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            DatabaseManager.instance.getPlayHistoryDao().updateSubtitleByKey(
                uniqueKey, mediaType, null
            )
            onSourceChanged.invoke()
        }
    }
}