package com.xyoye.common_component.weight.dialog

import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.viewModelScope
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.R
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.database.DatabaseManager
import com.xyoye.common_component.extension.resumeWhenAlive
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.bean.StorageFileBean
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import kotlinx.coroutines.*
import kotlin.coroutines.resume


/**
 * Created by xyoye on 2022/1/18
 */
object ExtraSourceDialogUtils {
    fun show(
        activity: BaseActivity<*, *>,
        mediaType: MediaType,
        data: StorageFileBean,
        options: ActivityOptionsCompat,
        castFile: ((StorageFileBean, MediaLibraryEntity) -> Unit)?,
        onSourceChanged: () -> Unit,
    ): Boolean {
        val uniqueKey = data.uniqueKey
        if (uniqueKey.isNullOrEmpty()) {
            return false
        }

        val viewModelScope = activity.getOwnerViewModel().viewModelScope
        val actionList = createActionList(castFile != null, data)
        BottomActionDialog(activity, actionList) {
            when (it.actionId) {
                ManageAction.BIND_DANMU,
                ManageAction.BIND_SUBTITLE -> {
                    bindExtraSource(
                        activity,
                        mediaType,
                        data,
                        options,
                        it.actionId == ManageAction.BIND_DANMU
                    )
                }
                ManageAction.UNBIND_DANMU,
                ManageAction.UNBIND_SUBTITLE -> {
                    unbindExtraSource(
                        viewModelScope,
                        mediaType,
                        uniqueKey,
                        onSourceChanged,
                        it.actionId == ManageAction.UNBIND_DANMU
                    )
                }
                ManageAction.SCREENCAST -> provideVideo(
                    viewModelScope,
                    activity,
                    data,
                    castFile
                )
            }
            return@BottomActionDialog true
        }.show()

        return true
    }

    private fun createActionList(
        hasScreencast: Boolean,
        data: StorageFileBean
    ): MutableList<SheetActionBean> {
        val actionList = mutableListOf<SheetActionBean>()
        if (hasScreencast) {
            actionList.add(ManageAction.SCREENCAST.toAction())
        }
        actionList.add(ManageAction.BIND_DANMU.toAction())
        actionList.add(ManageAction.BIND_SUBTITLE.toAction())
        if (!data.danmuPath.isNullOrEmpty()) {
            actionList.add(ManageAction.UNBIND_DANMU.toAction())
        }
        if (!data.subtitlePath.isNullOrEmpty()) {
            actionList.add(ManageAction.UNBIND_SUBTITLE.toAction())
        }
        return actionList
    }

    /**
     * 绑定字幕或弹幕
     */
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

    /**
     * 解绑字幕或弹幕
     */
    private fun unbindExtraSource(
        scope: CoroutineScope,
        mediaType: MediaType,
        uniqueKey: String,
        onSourceChanged: () -> Unit,
        isUnbindDanmu: Boolean
    ) {
        if (isUnbindDanmu) {
            unbindDanmu(scope, mediaType, uniqueKey, onSourceChanged)
        } else {
            unbindSubtitle(scope, mediaType, uniqueKey, onSourceChanged)
        }
    }

    /**
     * 解绑弹幕
     */
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

    /**
     * 解绑字幕
     */
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

    /**
     * 投屏
     */
    private fun provideVideo(
        scope: CoroutineScope,
        activity: BaseActivity<*, *>,
        data: StorageFileBean,
        castFile: ((StorageFileBean, MediaLibraryEntity) -> Unit)?
    ) {
        scope.launch(Dispatchers.IO) {
            //获取所有可用的投屏设备
            val screencastDevices = DatabaseManager.instance.getMediaLibraryDao()
                .getByMediaTypeSuspend(MediaType.SCREEN_CAST)
            if (screencastDevices.isEmpty()) {
                ToastCenter.showError("无可用投屏设备")
                return@launch
            }
            //选择投屏设备
            val screencastDevice = withContext(Dispatchers.Main) {
                selectScreencastDevice(activity, screencastDevices)
            } ?: return@launch

            withContext(Dispatchers.Main) {
                castFile?.invoke(data, screencastDevice)
            }
        }
    }

    private suspend fun selectScreencastDevice(
        activity: BaseActivity<*, *>,
        devices: MutableList<MediaLibraryEntity>
    ) = suspendCancellableCoroutine { continuation ->
        //投屏设备不存在
        if (devices.isEmpty()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        //仅有一个投屏设备，弹窗确认投屏
        if (devices.size == 1) {
            continuation.resume(devices[0])
            return@suspendCancellableCoroutine
        }
        //存在多个投屏设备，选择设备
        BottomActionDialog(
            title = "选择投屏设备",
            activity = activity,
            actionData = devices.map {
                SheetActionBean(
                    it.id,
                    it.displayName,
                    R.drawable.ic_screencast_device,
                    it.url
                )
            }.toMutableList()
        ) {
            val device = devices.firstOrNull { device -> device.id == it.actionId }
            continuation.resume(device)
            return@BottomActionDialog true
        }.apply {
            setOnDismissListener {
                continuation.resumeWhenAlive(null)
            }
            show()
        }
    }

    private enum class ManageAction(val title: String, val icon: Int) {
        SCREENCAST("投屏", R.drawable.ic_video_cast),
        BIND_DANMU("手动查找弹幕", R.drawable.ic_bind_danmu_manual),
        BIND_SUBTITLE("手动查找字幕", R.drawable.ic_bind_subtitle),
        UNBIND_DANMU("移除弹幕绑定", R.drawable.ic_unbind_danmu),
        UNBIND_SUBTITLE("移除字幕绑定", R.drawable.ic_unbind_subtitle);

        fun toAction() = SheetActionBean(this, title, icon)
    }
}