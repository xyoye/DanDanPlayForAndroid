package com.xyoye.stream_component.ui.activities.web_dav_file

import android.view.KeyEvent
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.thegrizzlylabs.sardineandroid.DavResource
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.databinding.ItemFileManagerPathBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.utils.view.FilePathItemDecoration
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityWebDavFileBinding
import com.xyoye.stream_component.databinding.ItemStorageFolderBinding

@Route(path = RouteTable.Stream.WebDavFile)
class WebDavFileActivity : BaseActivity<WebDavFileViewModel, ActivityWebDavFileBinding>() {

    @Autowired
    @JvmField
    var webDavData: MediaLibraryEntity? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            WebDavFileViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_web_dav_file

    override fun initView() {
        ARouter.getInstance().inject(this)

        if (webDavData == null) {
            ToastCenter.showError("媒体库数据错误，请重试")
            title = "WebDav媒体库"
            return
        }
        title = webDavData!!.displayName

        initRv()

        viewModel.pathLiveData.observe(this) {
            dataBinding.pathRv.setData(it)
        }
        viewModel.fileLiveData.observe(this) {
            dataBinding.fileRv.setData(it)
        }
        viewModel.openVideoLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .withParcelable("playParams", it)
                .navigation()
        }
        viewModel.listStorageRoot(webDavData!!)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (viewModel.openParentDirectory()) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initRv() {
        dataBinding.pathRv.apply {
            layoutManager = horizontal()

            adapter = buildAdapter<FilePathBean> {
                addItem<FilePathBean, ItemFileManagerPathBinding>(R.layout.item_file_manager_path) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            dirNameTv.text = data.name
                            dirNameTv.setTextColorRes(
                                if (data.isOpened) R.color.text_black else R.color.text_gray
                            )
                            dirNameTv.setOnClickListener {
                                viewModel.openDirectory(data.path)
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

            adapter = buildAdapter<DavResource> {
                addItem<DavResource, ItemStorageFolderBinding>(R.layout.item_storage_folder) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            fileNameTv.setAutoSizeText(data.displayName, 12, 17)
                            if (data.isDirectory) {
                                fileDescribeTv.text = "目录"
                                fileCoverIv.setImageResource(R.drawable.ic_folder)
                            } else {
                                fileDescribeTv.text = formatFileSize(data.contentLength)
                                fileCoverIv.setImageResource(MediaUtils.getMediaTypeCover(data.displayName))
                            }
                            fileDateTv.text = date2Str(data.modified, "yy-MM-dd HH:mm")
                            itemLayout.setOnClickListener {
                                when {
                                    data.isDirectory -> {
                                        viewModel.openDirectory(data.path)
                                    }
                                    else -> {
                                        openVideo(data)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openVideo(data: DavResource) {
        if (isVideoFile(data.name)) {
            viewModel.buildPlayParams(data)
        } else {
            ToastCenter.showWarning("不支持的视频文件格式")
        }
    }
}