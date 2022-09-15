package com.xyoye.common_component.weight.dialog

import android.content.Context
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.viewModelScope
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.R
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.services.ScreencastProvideService
import com.xyoye.common_component.source.VideoSourceManager
import com.xyoye.common_component.source.base.VideoSourceFactory
import com.xyoye.common_component.utils.FileComparator
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
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
    private const val ACTION_SCREENCAST = 5

    fun show(
        activity: BaseActivity<*, *>,
        mediaType: MediaType,
        data: StorageFileBean,
        options: ActivityOptionsCompat,
        screencastProvideService: ScreencastProvideService?,
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
        if (screencastProvideService != null) {
            actionList.add(
                SheetActionBean(
                    ACTION_SCREENCAST,
                    "投屏",
                    R.drawable.ic_unbind_subtitle
                )
            )
        }

        val viewModelScope = activity.getOwnerViewModel().viewModelScope
        BottomActionDialog(activity, actionList) {
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
                ACTION_SCREENCAST -> provideVideo(
                    viewModelScope,
                    activity,
                    data,
                    screencastProvideService,
                )
            }
            return@BottomActionDialog true
        }.show()

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

    private fun provideVideo(
        scope: CoroutineScope,
        context: Context,
        data: StorageFileBean,
        screencastProvideService: ScreencastProvideService?
    ) {
        if (screencastProvideService == null) {
            ToastCenter.showError("不支持投屏")
            return
        }

        scope.launch(Dispatchers.IO) {
            val mediaLibrary = DatabaseManager.instance.getMediaLibraryDao()
                .getByMediaTypeSuspend(MediaType.SCREEN_CAST)
            if (mediaLibrary == null) {
                ToastCenter.showError("请选择投屏设备")
                return@launch
            }
            val videoSources =
                DatabaseManager.instance.getVideoDao().getFolderVideoByFilePath(data.filePath)
            videoSources.sortWith(FileComparator(
                value = { getFileName(it.filePath) },
                isDirectory = { false }
            ))

            //如果视频地址对应的目录下找不到，可能视频已经被移除
            val index = videoSources.indexOfFirst { it.filePath == data.filePath }
            if (index == -1) {
                ToastCenter.showError("播放失败，找不到播放资源")
                return@launch
            }

            val mediaSource = VideoSourceFactory.Builder()
                .setVideoSources(videoSources)
                .setIndex(index)
                .create(MediaType.LOCAL_STORAGE)

            if (mediaSource == null) {
                ToastCenter.showError("播放失败，找不到播放资源")
                return@launch
            }
            VideoSourceManager.getInstance().setSource(mediaSource)
            screencastProvideService.startService(context, mediaLibrary)
        }
    }
}