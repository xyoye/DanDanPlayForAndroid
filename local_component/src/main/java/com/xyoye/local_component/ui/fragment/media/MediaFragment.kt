package com.xyoye.local_component.ui.fragment.media

import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.application.DanDanPlay
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.services.ScreencastProvideService
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.FragmentMediaBinding
import com.xyoye.local_component.databinding.ItemMediaLibraryBinding
import com.xyoye.local_component.utils.getCover

/**
 * Created by xyoye on 2020/7/27.
 */

@Route(path = RouteTable.Local.MediaFragment)
class MediaFragment : BaseFragment<MediaViewModel, FragmentMediaBinding>() {
    companion object {
        private const val ACTION_ADD_FTP_LIBRARY = 1
        private const val ACTION_ADD_SMB_LIBRARY = 2
        private const val ACTION_ADD_WEBDAV_LIBRARY = 3
        private const val ACTION_ADD_REMOTE_LIBRARY = 4
        private const val ACTION_ADD_SCREENCAST_DEVICE = 5
        private const val ACTION_ADD_EXTERNAL_LIBRARY = 6


        private const val ACTION_EDIT_STORAGE = 11
        private const val ACTION_DELETE_STORAGE = 12
    }

    @Autowired
    lateinit var provideService: ScreencastProvideService

    override fun initViewModel() = ViewModelInit(
        BR.viewModel,
        MediaViewModel::class.java
    )

    override fun getLayoutId() = R.layout.fragment_media

    override fun initView() {
        ARouter.getInstance().inject(this)

        viewModel.initLocalStorage()

        initRv()

        dataBinding.addMediaStorageBt.setOnClickListener {
            addMediaStorage()
        }

        viewModel.mediaLibWithStatusLiveData.observe(this) {
            dataBinding.mediaLibRv.setData(it)
        }
    }

    private fun initRv() {
        dataBinding.mediaLibRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addItem<MediaLibraryEntity, ItemMediaLibraryBinding>(R.layout.item_media_library) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            libraryNameTv.text = data.displayName
                            libraryUrlTv.text = when (data.mediaType) {
                                MediaType.STREAM_LINK,
                                MediaType.MAGNET_LINK,
                                MediaType.REMOTE_STORAGE,
                                MediaType.SMB_SERVER,
                                MediaType.EXTERNAL_STORAGE -> data.describe
                                else -> data.url
                            }
                            libraryCoverIv.setImageResource(data.mediaType.getCover())

                            screencastStatusTv.isVisible = data.mediaType == MediaType.SCREEN_CAST && data.running
                            screencastStatusTv.setOnClickListener {
                                showStopServiceDialog()
                            }

                            itemLayout.setOnClickListener {
                                DanDanPlay.permission.storage.request(this@MediaFragment) {
                                    onGranted {
                                        launchMediaStorage(data)
                                    }
                                    onDenied {
                                        ToastCenter.showError("获取文件读取权限失败，无法打开媒体库")
                                    }
                                }
                            }
                            itemLayout.setOnLongClickListener {
                                if (data.mediaType == MediaType.WEBDAV_SERVER
                                    || data.mediaType == MediaType.FTP_SERVER
                                    || data.mediaType == MediaType.SMB_SERVER
                                    || data.mediaType == MediaType.REMOTE_STORAGE
                                    || data.mediaType == MediaType.SCREEN_CAST
                                    || data.mediaType == MediaType.EXTERNAL_STORAGE
                                ) {
                                    showEditStorageDialog(data)
                                }
                                true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addMediaStorage() {
        BottomActionDialog(
            requireActivity(),
            mutableListOf(
                SheetActionBean(
                    ACTION_ADD_FTP_LIBRARY,
                    "FTP媒体库",
                    MediaType.FTP_SERVER.getCover()
                ),
                SheetActionBean(
                    ACTION_ADD_SMB_LIBRARY,
                    "SMB媒体库",
                    MediaType.SMB_SERVER.getCover()
                ),
                SheetActionBean(
                    ACTION_ADD_WEBDAV_LIBRARY,
                    "WebDav媒体库",
                    MediaType.WEBDAV_SERVER.getCover()
                ),
                SheetActionBean(
                    ACTION_ADD_REMOTE_LIBRARY,
                    "远程媒体库",
                    MediaType.REMOTE_STORAGE.getCover()
                ),
                SheetActionBean(
                    ACTION_ADD_SCREENCAST_DEVICE,
                    "投屏设备",
                    MediaType.SCREEN_CAST.getCover()
                ),
                SheetActionBean(
                    ACTION_ADD_EXTERNAL_LIBRARY,
                    "设备存储库",
                    MediaType.EXTERNAL_STORAGE.getCover()
                )
            ),
            "新增网络媒体库"
        ) {
            val routePath = when (it) {
                ACTION_ADD_WEBDAV_LIBRARY -> RouteTable.Stream.WebDavLogin
                ACTION_ADD_FTP_LIBRARY -> RouteTable.Stream.FTPLogin
                ACTION_ADD_SMB_LIBRARY -> RouteTable.Stream.SmbLogin
                ACTION_ADD_REMOTE_LIBRARY -> RouteTable.Stream.RemoteLogin
                ACTION_ADD_SCREENCAST_DEVICE -> RouteTable.Stream.ScreencastConnect
                ACTION_ADD_EXTERNAL_LIBRARY -> RouteTable.Stream.DocumentTree
                else -> throw IllegalArgumentException()
            }

            ARouter.getInstance()
                .build(routePath)
                .navigation()

            return@BottomActionDialog true
        }.show()
    }

    private fun launchMediaStorage(data: MediaLibraryEntity) {
        when (data.mediaType) {
            MediaType.LOCAL_STORAGE -> {
                ARouter.getInstance()
                    .build(RouteTable.Local.LocalMediaStorage)
                    .navigation()
            }
            MediaType.STREAM_LINK, MediaType.MAGNET_LINK, MediaType.OTHER_STORAGE -> {
                ARouter.getInstance()
                    .build(RouteTable.Local.PlayHistory)
                    .withSerializable("typeValue", data.mediaType.value)
                    .navigation()
            }
            MediaType.WEBDAV_SERVER -> {
                ARouter.getInstance()
                    .build(RouteTable.Stream.WebDavFile)
                    .withParcelable("webDavData", data)
                    .navigation()
            }
            MediaType.FTP_SERVER -> {
                ARouter.getInstance()
                    .build(RouteTable.Stream.FTPFile)
                    .withParcelable("ftpData", data)
                    .navigation()
            }
            MediaType.SMB_SERVER -> {
                ARouter.getInstance()
                    .build(RouteTable.Stream.SmbFile)
                    .withParcelable("smbData", data)
                    .navigation()
            }
            MediaType.REMOTE_STORAGE -> {
                ARouter.getInstance()
                    .build(RouteTable.Stream.RemoteFile)
                    .withParcelable("remoteData", data)
                    .navigation()
            }
            MediaType.SCREEN_CAST -> {
                viewModel.checkScreenDeviceRunning(data)
            }
            MediaType.EXTERNAL_STORAGE -> {
                ARouter.getInstance()
                    .build(RouteTable.Stream.StorageFile)
                    .withParcelable("storageLibrary", data)
                    .navigation()
            }
        }
    }

    private fun showEditStorageDialog(data: MediaLibraryEntity) {
        val actions = mutableListOf<SheetActionBean>()
        if (data.mediaType != MediaType.EXTERNAL_STORAGE) {
            actions.add(
                SheetActionBean(
                    ACTION_EDIT_STORAGE,
                    "编辑媒体库",
                    R.drawable.ic_edit_storage
                )
            )
        }
        actions.add(
            SheetActionBean(
                ACTION_DELETE_STORAGE,
                "删除媒体库",
                R.drawable.ic_delete_storage
            )
        )
        BottomActionDialog(requireActivity(), actions) {
            if (it == ACTION_EDIT_STORAGE) {
                val routePath = when (data.mediaType) {
                    MediaType.WEBDAV_SERVER -> RouteTable.Stream.WebDavLogin
                    MediaType.FTP_SERVER -> RouteTable.Stream.FTPLogin
                    MediaType.SMB_SERVER -> RouteTable.Stream.SmbLogin
                    MediaType.REMOTE_STORAGE -> RouteTable.Stream.RemoteLogin
                    MediaType.SCREEN_CAST -> RouteTable.Stream.ScreencastConnect
                    else -> throw IllegalArgumentException()
                }
                ARouter.getInstance()
                    .build(routePath)
                    .withParcelable("editData", data)
                    .navigation()
            } else if (it == ACTION_DELETE_STORAGE) {
                showDeleteStorageDialog(data)
            }
            return@BottomActionDialog true
        }.show()
    }

    private fun showDeleteStorageDialog(data: MediaLibraryEntity) {
        CommonDialog.Builder(requireActivity())
            .apply {
                content = "确认删除以下媒体库?\n\n${data.displayName}"
                positiveText = "确认"
                addPositive { dialog ->
                    dialog.dismiss()
                    viewModel.deleteStorage(data)
                }
                addNegative()
            }.build().show()
    }

    private fun showStopServiceDialog() {
        CommonDialog.Builder(requireActivity())
            .apply {
                content = "确认停止投屏投送服务？"
                positiveText = "确认"
                addPositive { dialog ->
                    dialog.dismiss()
                    provideService.stopService(requireActivity())
                }
                addNegative()
            }.build().show()
    }
}