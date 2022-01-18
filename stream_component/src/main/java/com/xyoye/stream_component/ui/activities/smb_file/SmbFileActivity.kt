package com.xyoye.stream_component.ui.activities.smb_file

import android.content.Intent
import android.view.KeyEvent
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.databinding.ItemFileManagerPathBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.common_component.utils.smb.SMBFile
import com.xyoye.common_component.utils.view.FilePathItemDecoration
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivitySmbFileBinding
import com.xyoye.stream_component.databinding.ItemStorageFolderV2Binding
import com.xyoye.stream_component.databinding.ItemStorageVideoBinding
import com.xyoye.stream_component.ui.dialog.UnBindSourceDialogUtils

@Route(path = RouteTable.Stream.SmbFile)
class SmbFileActivity : BaseActivity<SmbFileViewModel, ActivitySmbFileBinding>() {
    companion object {
        private const val PLAY_REQUEST_CODE = 1001
    }

    @Autowired
    @JvmField
    var smbData: MediaLibraryEntity? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            SmbFileViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_smb_file

    override fun initView() {
        ARouter.getInstance().inject(this)

        if (smbData == null) {
            ToastCenter.showError("媒体库数据错误，请重试")
            title = "SMB媒体库"
            return
        }
        title = smbData!!.displayName

        dataBinding.refreshLayout.setOnRefreshListener {
            viewModel.refreshDirectory()
        }

        initRv()

        initObserver()

        viewModel.initFtp(smbData!!)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDirectoryWithHistory()
    }

    override fun observeLoadingDialog() {
        //替换弹窗观察者
        viewModel.loadingObserver.observe(this, {
            if (dataBinding.refreshLayout.isRefreshing) {
                dataBinding.refreshLayout.isRefreshing = false
            }

            if (it.first > 0) {
                dataBinding.refreshLayout.isRefreshing = true
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (viewModel.openParentDirectory()) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLAY_REQUEST_CODE) {
            viewModel.closeStream()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        viewModel.closeSMB()
        super.onDestroy()
    }

    private fun initRv() {
        dataBinding.pathRv.apply {
            layoutManager = horizontal()

            adapter = buildAdapter {
                addItem<FilePathBean, ItemFileManagerPathBinding>(R.layout.item_file_manager_path) {
                    initView { data, position, _ ->
                        itemBinding.apply {
                            dirNameTv.text = data.name
                            dirNameTv.setTextColorRes(
                                if (data.isOpened) R.color.text_black else R.color.text_gray
                            )
                            dirNameTv.setOnClickListener {
                                viewModel.openPositionDirectory(position)
                            }
                        }
                    }
                }
            }

            val dividerSize = dp2px(16)
            val divider = R.drawable.ic_file_manager_arrow.toResDrawable()
            if (divider != null) {
                addItemDecoration(FilePathItemDecoration(divider, dividerSize))
            }
        }

        dataBinding.fileRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addItem<SMBFile, ItemStorageVideoBinding>(R.layout.item_storage_video) {
                    checkType { data, _ -> data.isDirectory.not() }

                    initView { data, _, _ ->
                        val progressText = if (data.position > 0 && data.duration > 0) {
                            "${formatDuration(data.position)}/${formatDuration(data.duration)}"
                        } else if (data.duration > 0) {
                            formatDuration(data.duration)
                        } else {
                            ""
                        }

                        itemBinding.coverIv.setVideoCover(data.uniqueKey)
                        itemBinding.titleTv.text = data.name
                        itemBinding.durationTv.text = progressText
                        itemBinding.durationTv.isVisible = data.duration > 0
                        itemBinding.danmuTipsTv.isGone = data.danmuPath.isNullOrEmpty()
                        itemBinding.subtitleTipsTv.isGone = data.subtitlePath.isNullOrEmpty()
                        itemBinding.moreActionIv.isGone =
                            data.danmuPath.isNullOrEmpty() && data.subtitlePath.isNullOrEmpty()

                        itemBinding.itemLayout.setOnClickListener {
                            viewModel.openVideoFile(data)
                        }
                        itemBinding.moreActionIv.setOnClickListener {
                            showVideoManagerDialog(data)
                        }
                        itemBinding.itemLayout.setOnLongClickListener {
                            showVideoManagerDialog(data)
                        }
                    }
                }

                addItem<SMBFile, ItemStorageFolderV2Binding>(R.layout.item_storage_folder_v2) {
                    checkType { data, _ -> data.isDirectory }
                    initView { data, _, _ ->
                        itemBinding.folderTv.text = data.name
                        itemBinding.fileCountTv.text = "目录"
                        itemBinding.itemLayout.setOnClickListener {
                            viewModel.openChildDirectory(data.name)
                        }
                    }
                }
            }
        }
    }

    private fun initObserver() {
        viewModel.pathLiveData.observe(this) {
            dataBinding.pathRv.setData(it)
        }

        viewModel.fileLiveData.observe(this) {
            dataBinding.fileRv.setData(it)
        }

        viewModel.playLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation(this, PLAY_REQUEST_CODE)
        }
    }

    private fun showVideoManagerDialog(smbFile: SMBFile): Boolean {
        return UnBindSourceDialogUtils.show(
            this@SmbFileActivity,
            viewModel.viewModelScope,
            MediaType.SMB_SERVER,
            smbFile.uniqueKey,
            smbFile.danmuPath,
            smbFile.subtitlePath,
            afterUnbindSource = {
                viewModel.refreshDirectoryWithHistory()
            }
        )
    }
}