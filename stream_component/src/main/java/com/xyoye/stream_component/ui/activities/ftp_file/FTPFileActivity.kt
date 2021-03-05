package com.xyoye.stream_component.ui.activities.ftp_file

import android.content.Intent
import android.view.KeyEvent
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.databinding.ItemFileManagerPathBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.utils.view.FilePathItemDecoration
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityFtpFileBinding
import com.xyoye.stream_component.databinding.ItemStorageFolderBinding
import org.apache.commons.net.ftp.FTPFile

@Route(path = RouteTable.Stream.FTPFile)
class FTPFileActivity : BaseActivity<FTPFileViewModel, ActivityFtpFileBinding>() {
    companion object {
        private const val PLAY_REQUEST_CODE = 1001
    }

    @Autowired
    @JvmField
    var ftpData: MediaLibraryEntity? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            FTPFileViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_ftp_file

    override fun initView() {
        ARouter.getInstance().inject(this)

        if (ftpData == null) {
            ToastCenter.showError("媒体库数据错误，请重试")
            title = "FTP媒体库"
            return
        }
        title = ftpData!!.displayName

        dataBinding.refreshLayout.setOnRefreshListener {
            viewModel.refreshDirectory()
        }

        initRv()

        initObserver()

        viewModel.initFtp(ftpData!!)
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
            viewModel.closeFTP()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        viewModel.closeFTP()
        super.onDestroy()
    }

    private fun initRv() {
        dataBinding.pathRv.apply {
            layoutManager = horizontal()

            adapter = buildAdapter<FilePathBean> {
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
            val divider = getResDrawable(R.drawable.ic_file_manager_arrow)
            if (divider != null) {
                addItemDecoration(FilePathItemDecoration(divider, dividerSize))
            }
        }

        dataBinding.fileRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter<FTPFile> {
                addItem<FTPFile, ItemStorageFolderBinding>(R.layout.item_storage_folder) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            fileNameTv.setAutoSizeText(data.name, 12, 17)
                            if (data.isDirectory) {
                                fileDescribeTv.text = "目录"
                                fileCoverIv.setImageResource(R.drawable.ic_folder)
                            } else {
                                fileDescribeTv.text = formatFileSize(data.size)
                                fileCoverIv.setImageResource(MediaUtils.getMediaTypeCover(data.name))
                            }
                            fileDateTv.text = date2Str(data.timestamp.time, "yy-MM-dd HH:mm")
                            itemLayout.setOnClickListener {
                                if (data.isDirectory) {
                                    viewModel.openChildDirectory(data.name)
                                } else {
                                    openVideo(data)
                                }
                            }
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

        viewModel.playVideoLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .withParcelable("playParams", it)
                .navigation(this, PLAY_REQUEST_CODE)
        }
    }

    private fun openVideo(ftpFile: FTPFile){
        //仅支持视频文件
        if (!isVideoFile(ftpFile.name)) {
            ToastCenter.showWarning("不支持的视频文件格式")
            return
        }

        val showTips = AppConfig.isShowFTPVideoTips()
        if (!showTips){
            viewModel.openVideoFile(ftpFile.name, ftpFile.size)
            return
        }

        CommonDialog.Builder().run {
            content = "FTP视频播放不能调整进度至未缓冲位置，请谨慎调整视频进度"
            addNegative()
            addPositive{
                it.dismiss()
                viewModel.openVideoFile(ftpFile.name, ftpFile.size)
            }
            addNoShowAgain { AppConfig.putShowFTPVideoTips(!it) }
            build()
        }.show(this@FTPFileActivity)
    }
}