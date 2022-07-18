package com.xyoye.common_component.weight.dialog

import androidx.core.app.ActivityOptionsCompat
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
        options: ActivityOptionsCompat,
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
        BottomActionDialog(actionList) {
            when (it) {
                ACTION_BIND_DANMU -> bindExtraSource(activity, mediaType, data, options, true)
                ACTION_BIND_SUBTITLE -> bindExtraSource(activity, mediaType, data, options, false)
                ACTION_UNBIND_DANMU -> unbindDanmu(
                    viewModelScope,
                    mediaType,
                    uniqueKey,
                    onSourceChanged
                )
                ACTION_UNBIND_SUBTITLE -> unbindSubtitle(
                    viewModelScope,
                    mediaType,
                    uniqueKey,
                    onSourceChanged
                )
            }
            return@BottomActionDialog true
        }.show(activity)

        return true
    }

    private fun bindExtraSource(
        activity: BaseActivity<*, *>,
        mediaType: MediaType,
        data: StorageFileBean,
        options: ActivityOptionsCompat,
        isSearchDanmu: Boolean,
    ) {
        val videoPath = if (mediaType == MediaType.LOCAL_STORAGE) {
            data.filePath
        } else {
            null
        }
        ARouter.getInstance()
            .build(RouteTable.Local.BindExtraSource)
            .withBoolean("isSearchDanmu", isSearchDanmu)
            .withString("videoPath", videoPath)
            .withString("videoTitle", data.fileName)
            .withString("uniqueKey", data.uniqueKey)
            .withString("mediaType", mediaType.value)
            .withString("fileCoverUrl", data.fileCoverUrl)
            .withOptionsCompat(options)
            .navigation(activity)
    }

    private fun unbindDanmu(
        scope: CoroutineScope,
        mediaType: MediaType,
        uniqueKey: String,
        onSourceChanged: () -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            DatabaseManager.instance.getPlayHistoryDao().updateDanmu(
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
            DatabaseManager.instance.getPlayHistoryDao().updateSubtitle(
                uniqueKey, mediaType, null
            )
            onSourceChanged.invoke()
        }
    }
}